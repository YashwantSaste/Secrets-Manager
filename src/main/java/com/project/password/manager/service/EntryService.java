package com.project.password.manager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.project.password.manager.database.DataRepository;
import com.project.password.manager.database.EntryDataRepository;
import com.project.password.manager.database.EntryStorageKey;
import com.project.password.manager.encryption.IEncryptionService;
import com.project.password.manager.event.EventType;
import com.project.password.manager.event.listener.IPersistenceEventListener;
import com.project.password.manager.event.listener.PersistenceListenerFactory;
import com.project.password.manager.exceptions.EntityNotFoundException;
import com.project.password.manager.exceptions.UnauthorizedSessionException;
import com.project.password.manager.model.IVault;
import com.project.password.manager.model.entry.EncryptedEntryRecord;
import com.project.password.manager.model.entry.EntrySecretPayload;
import com.project.password.manager.model.entry.EntryUpsertRequest;
import com.project.password.manager.model.entry.EntryView;
import com.project.password.manager.model.entry.NoteValue;
import com.project.password.manager.model.entry.TagValue;
import com.project.password.manager.util.ModelObjectMapperFactory;
import com.project.password.manager.util.ValidationUtils;

public class EntryService {

	@NotNull
	private final EntryDataRepository entryRepository;
	@NotNull
	private final DataRepository<IVault, String> vaultRepository;
	@NotNull
	private final IEncryptionService encryptionService;
	@NotNull
	private final ObjectMapper objectMapper;
	@NotNull
	private final PersistenceListenerFactory listenerFactory;
	@NotNull
	private final Cache<String, VaultSearchIndex> searchIndexByVault = Caffeine.newBuilder()
	.expireAfterAccess(20, TimeUnit.MINUTES).maximumSize(200).build();

	public EntryService(@NotNull EntryDataRepository entryRepository,
			@NotNull DataRepository<IVault, String> vaultRepository,
			@NotNull IEncryptionService encryptionService, @NotNull PersistenceListenerFactory listenerFactory) {
		this.entryRepository = entryRepository;
		this.vaultRepository = vaultRepository;
		this.encryptionService = encryptionService;
		this.objectMapper = ModelObjectMapperFactory.create();
		this.listenerFactory = listenerFactory;
	}

	@NotNull
	public EntryView createEntry(@NotNull String userId, @NotNull String vaultId,
			@NotNull EntryUpsertRequest request) {
		ValidationUtils.validate(request);
		assertOwnedVault(userId, vaultId);
		String entryId = UUID.randomUUID().toString();
		long now = System.currentTimeMillis();
		EncryptedEntryRecord record = new EncryptedEntryRecord();
		record.setId(entryId);
		record.setVaultId(vaultId);
		record.setCreatedAtEpochMs(now);
		record.setUpdatedAtEpochMs(now);
		record.setEncryptedPayload(encryptPayload(userId, toSecretPayload(request)));
		entryRepository.save(record);
		IPersistenceEventListener<EncryptedEntryRecord> listener = listenerFactory.create(record, EventType.CREATE);
		listener.onEvent(listener.createEvent());
		EntryView view = toView(record, request);
		upsertIndex(view);
		return view;
	}

	@NotNull
	public EntryView getEntry(@NotNull String userId, @NotNull String vaultId, @NotNull String entryId) {
		assertOwnedVault(userId, vaultId);
		EncryptedEntryRecord record = requireRecord(vaultId, entryId);
		return decryptRecord(userId, record);
	}

	@NotNull
	public List<EntryView> getEntriesByReference(@NotNull String userId, @NotNull List<String> vaultIds,
			@NotNull String entryReference) {
		String normalizedReference = requireText(entryReference, "Entry reference");
		List<EntryView> matchedEntries = new ArrayList<>();
		for (String vaultId : vaultIds) {
			assertOwnedVault(userId, vaultId);
			EncryptedEntryRecord record = entryRepository.findById(new EntryStorageKey(vaultId, normalizedReference));
			if (record != null) {
				matchedEntries.add(decryptRecord(userId, record));
				continue;
			}
			for (EntryView entry : loadIndex(userId, vaultId).orderedEntries()) {
				if (normalizedReference.equalsIgnoreCase(entry.getLabel())) {
					matchedEntries.add(entry);
				}
			}
		}
		if (matchedEntries.isEmpty()) {
			throw new EntityNotFoundException("The entry with reference " + normalizedReference + " does not exist");
		}
		matchedEntries.sort(ENTRY_RECENCY_COMPARATOR);
		return Collections.unmodifiableList(matchedEntries);
	}

	@NotNull
	public List<EntryView> getEntries(@NotNull String userId, @NotNull String vaultId) {
		assertOwnedVault(userId, vaultId);
		List<EntryView> entries = new ArrayList<>(loadIndex(userId, vaultId).orderedEntries());
		entries.sort(ENTRY_RECENCY_COMPARATOR);
		return Collections.unmodifiableList(entries);
	}

	@NotNull
	public EntryView updateEntry(@NotNull String userId, @NotNull String vaultId, @NotNull String entryId,
			@NotNull EntryUpsertRequest request) {
		ValidationUtils.validate(request);
		assertOwnedVault(userId, vaultId);
		EncryptedEntryRecord record = requireRecord(vaultId, entryId);
		IPersistenceEventListener<EncryptedEntryRecord> listener = listenerFactory.create(record, EventType.UPDATE);
		record.setUpdatedAtEpochMs(System.currentTimeMillis());
		record.setEncryptedPayload(encryptPayload(userId, toSecretPayload(request)));
		entryRepository.update(new EntryStorageKey(vaultId, entryId), record);
		listener.onUpdate(record);
		EntryView view = toView(record, request);
		upsertIndex(view);
		return view;
	}

	public void deleteEntry(@NotNull String userId, @NotNull String vaultId, @NotNull String entryId) {
		assertOwnedVault(userId, vaultId);
		EncryptedEntryRecord record = requireRecord(vaultId, entryId);
		IPersistenceEventListener<EncryptedEntryRecord> listener = listenerFactory.create(record, EventType.DELETE);
		entryRepository.delete(new EntryStorageKey(vaultId, entryId));
		removeFromIndex(vaultId, entryId);
		listener.onEvent(listener.createEvent());
	}

	@NotNull
	public List<EntryView> searchEntries(@NotNull String userId, @NotNull String vaultId, @NotNull String query) {
		assertOwnedVault(userId, vaultId);
		String normalizedQuery = normalize(query);
		if (normalizedQuery.isEmpty()) {
			return Collections.emptyList();
		}
		VaultSearchIndex index = loadIndex(userId, vaultId);
		List<String> queryTokens = tokenize(normalizedQuery);
		Set<String> matchedIds = new HashSet<>();
		for (String token : queryTokens) {
			Set<String> tokenMatches = index.invertedIndex.get(token);
			if (tokenMatches == null || tokenMatches.isEmpty()) {
				return Collections.emptyList();
			}
			if (matchedIds.isEmpty()) {
				matchedIds.addAll(tokenMatches);
			} else {
				matchedIds.retainAll(tokenMatches);
			}
			if (matchedIds.isEmpty()) {
				return Collections.emptyList();
			}
		}
		List<EntryView> results = new ArrayList<>();
		for (EntryView entry : index.entriesById.values()) {
			if (matchedIds.contains(entry.getId())) {
				results.add(entry);
			}
		}
		results.sort(ENTRY_RECENCY_COMPARATOR);
		return results;
	}

	@NotNull
	private static String requireText(@NotNull String value, @NotNull String fieldName) {
		String normalizedValue = value.trim();
		if (normalizedValue.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
		return normalizedValue;
	}

	private void assertOwnedVault(@NotNull String userId, @NotNull String vaultId) {
		IVault vault = vaultRepository.findById(vaultId);
		if (vault == null) {
			throw new EntityNotFoundException("The vault with id " + vaultId + " does not exist");
		}
		if (!userId.equals(vault.getUserId())) {
			throw new UnauthorizedSessionException("The vault with id " + vaultId + " does not belong to user " + userId);
		}
	}

	@NotNull
	private EncryptedEntryRecord requireRecord(@NotNull String vaultId, @NotNull String entryId) {
		EncryptedEntryRecord record = entryRepository.findById(new EntryStorageKey(vaultId, entryId));
		if (record == null) {
			throw new EntityNotFoundException("The entry with id " + entryId + " does not exist");
		}
		return record;
	}

	@NotNull
	private String encryptPayload(@NotNull String userId, @NotNull EntrySecretPayload payload) {
		try {
			return encryptionService.encrypt(objectMapper.writeValueAsString(payload), userId);
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to encrypt entry payload for user " + userId, ex);
		}
	}

	@NotNull
	private EntryView decryptRecord(@NotNull String userId, @NotNull EncryptedEntryRecord record) {
		try {
			String rawPayload = encryptionService.decrypt(record.getEncryptedPayload(), userId);
			EntrySecretPayload payload = objectMapper.readValue(rawPayload, EntrySecretPayload.class);
			EntryView view = new EntryView();
			view.setId(record.getId());
			view.setVaultId(record.getVaultId());
			view.setCreatedAtEpochMs(record.getCreatedAtEpochMs());
			view.setUpdatedAtEpochMs(record.getUpdatedAtEpochMs());
			view.setLabel(payload.getLabel());
			view.setPassword(payload.getPassword());
			view.setUsername(payload.getUsername());
			view.setLoginName(payload.getLoginName());
			view.setUrl(payload.getUrl());
			view.setNotes(payload.getNotes());
			view.setTags(payload.getTags());
			return view;
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to decrypt entry payload for entry " + record.getId(), ex);
		}
	}

	@NotNull
	private EntrySecretPayload toSecretPayload(@NotNull EntryUpsertRequest request) {
		EntrySecretPayload payload = new EntrySecretPayload();
		payload.setLabel(request.getLabel().trim());
		payload.setPassword(request.getPassword());
		payload.setUsername(trimToNull(request.getUsername()));
		payload.setLoginName(trimToNull(request.getLoginName()));
		payload.setUrl(trimToNull(request.getUrl()));
		payload.setNotes(request.getNotes() == null ? new ArrayList<>() : new ArrayList<>(request.getNotes()));
		payload.setTags(request.getTags() == null ? new ArrayList<>() : new ArrayList<>(request.getTags()));
		return payload;
	}

	@NotNull
	private EntryView toView(@NotNull EncryptedEntryRecord record, @NotNull EntryUpsertRequest request) {
		EntryView view = new EntryView();
		view.setId(record.getId());
		view.setVaultId(record.getVaultId());
		view.setCreatedAtEpochMs(record.getCreatedAtEpochMs());
		view.setUpdatedAtEpochMs(record.getUpdatedAtEpochMs());
		view.setLabel(request.getLabel().trim());
		view.setPassword(request.getPassword());
		view.setUsername(trimToNull(request.getUsername()));
		view.setLoginName(trimToNull(request.getLoginName()));
		view.setUrl(trimToNull(request.getUrl()));
		view.setNotes(request.getNotes() == null ? new ArrayList<>() : new ArrayList<>(request.getNotes()));
		view.setTags(request.getTags() == null ? new ArrayList<>() : new ArrayList<>(request.getTags()));
		return view;
	}

	@NotNull
	private VaultSearchIndex loadIndex(@NotNull String userId, @NotNull String vaultId) {
		return searchIndexByVault.get(vaultId, ignored -> buildIndex(userId, vaultId));
	}

	@NotNull
	private VaultSearchIndex buildIndex(@NotNull String userId, @NotNull String vaultId) {
		VaultSearchIndex index = new VaultSearchIndex();
		for (EncryptedEntryRecord record : entryRepository.findByVaultId(vaultId)) {
			EntryView view = decryptRecord(userId, record);
			index.add(view);
		}
		return index;
	}

	private void upsertIndex(@NotNull EntryView entry) {
		VaultSearchIndex index = searchIndexByVault.getIfPresent(entry.getVaultId());
		if (index != null) {
			index.add(entry);
		}
	}

	private void removeFromIndex(@NotNull String vaultId, @NotNull String entryId) {
		VaultSearchIndex index = searchIndexByVault.getIfPresent(vaultId);
		if (index != null) {
			index.remove(entryId);
		}
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String normalizedValue = value.trim();
		return normalizedValue.isEmpty() ? null : normalizedValue;
	}

	@NotNull
	private static String normalize(@NotNull String input) {
		return input.toLowerCase(Locale.ROOT).trim();
	}

	@NotNull
	private static List<String> tokenize(@NotNull String input) {
		List<String> tokens = new ArrayList<>();
		for (String part : input.split("[^a-z0-9]+")) {
			if (!part.isBlank()) {
				tokens.add(part);
			}
		}
		return tokens;
	}

	@NotNull
	private static final Comparator<EntryView> ENTRY_RECENCY_COMPARATOR = Comparator
	.comparingLong(EntryView::getUpdatedAtEpochMs)
	.reversed()
	.thenComparing(Comparator.comparingLong(EntryView::getCreatedAtEpochMs).reversed())
	.thenComparing(EntryView::getId);

	private static final class VaultSearchIndex {

		private final Map<String, EntryView> entriesById = new HashMap<>();
		private final Map<String, Set<String>> invertedIndex = new HashMap<>();

		private void add(@NotNull EntryView entry) {
			remove(entry.getId());
			entriesById.put(entry.getId(), entry);
			for (String token : collectTokens(entry)) {
				invertedIndex.computeIfAbsent(token, ignored -> new HashSet<>()).add(entry.getId());
			}
		}

		private void remove(@NotNull String entryId) {
			EntryView existing = entriesById.remove(entryId);
			if (existing == null) {
				return;
			}
			for (String token : collectTokens(existing)) {
				Set<String> ids = invertedIndex.get(token);
				if (ids != null) {
					ids.remove(entryId);
					if (ids.isEmpty()) {
						invertedIndex.remove(token);
					}
				}
			}
		}

		@NotNull
		private List<EntryView> orderedEntries() {
			return new ArrayList<>(entriesById.values());
		}

		@NotNull
		private List<String> collectTokens(@NotNull EntryView entry) {
			List<String> tokens = new ArrayList<>();
			tokens.addAll(tokenize(normalize(entry.getLabel())));
			if (entry.getUsername() != null) {
				tokens.addAll(tokenize(normalize(entry.getUsername())));
			}
			if (entry.getLoginName() != null) {
				tokens.addAll(tokenize(normalize(entry.getLoginName())));
			}
			if (entry.getUrl() != null) {
				tokens.addAll(tokenize(normalize(entry.getUrl())));
			}
			for (TagValue tag : entry.getTags()) {
				tokens.addAll(tokenize(normalize(tag.getValue())));
			}
			for (NoteValue note : entry.getNotes()) {
				tokens.addAll(tokenize(normalize(note.getDescription())));
			}
			return tokens;
		}

	}

}
