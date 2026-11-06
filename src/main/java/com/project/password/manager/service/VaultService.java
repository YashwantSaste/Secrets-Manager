package com.project.password.manager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.password.manager.database.DataRepository;
import com.project.password.manager.encryption.IEncryptionService;
import com.project.password.manager.event.EventType;
import com.project.password.manager.event.listener.IPersistenceEventListener;
import com.project.password.manager.event.listener.PersistenceListenerFactory;
import com.project.password.manager.exceptions.EntityNotFoundException;
import com.project.password.manager.exceptions.UnauthorizedSessionException;
import com.project.password.manager.exceptions.UserNotFoundException;
import com.project.password.manager.guice.PlatformEntityProvider;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IVault;
import com.project.password.manager.model.Status;
import com.project.password.manager.model.VaultPayload;
import com.project.password.manager.util.ModelObjectMapperFactory;

public class VaultService {
	@NotNull
	private final DataRepository<IVault, String> vaultRepository;
	@NotNull
	private final DataRepository<IUser, String> userRepository;
	@NotNull
	private final IEncryptionService encryptionService;
	@NotNull
	private final ObjectMapper objectMapper;
	@NotNull
	private final PersistenceListenerFactory listenerFactory;

	public VaultService(@NotNull DataRepository<IUser, String> userRepository,
			@NotNull DataRepository<IVault, String> vaultRepository, @NotNull IEncryptionService encryptionService,
			@NotNull ObjectMapper objectMapper, @NotNull PersistenceListenerFactory listenerFactory) {
		this.userRepository = userRepository;
		this.vaultRepository = vaultRepository;
		this.encryptionService = encryptionService;
		this.objectMapper = objectMapper;
		this.listenerFactory = listenerFactory;
	}

	@NotNull
	public IVault createDefaultVault(@NotNull IUser user) {
		IPersistenceEventListener<IUser> userListener = listenerFactory.create(user, EventType.UPDATE);
		List<IVault> vaults = ensureVaultsInitialized(user);
		if (!vaults.isEmpty() || hasDefaultVault(user)) {
			throw new UnsupportedOperationException("A user can have only one default vault.");
		}
		IVault vault = createVault(user, vaults, "Default");
		user.setDefaultVaultId(vault.getId());
		persistUserIfPresent(user);
		IPersistenceEventListener<IVault> vaultListener = listenerFactory.create(vault, EventType.CREATE);
		vaultListener.onEvent(vaultListener.createEvent());
		userListener.onUpdate(user);
		return vault;
	}

	@NotNull
	public List<IVault> getAllVaults(@NotNull String userId) {
		return ensureVaultsInitialized(getUser(userId));
	}

	@NotNull
	public String createVaultForUser(@NotNull String userId, @NotNull String vaultName) {
		IUser user = getUser(userId);
		IPersistenceEventListener<IUser> userListener = listenerFactory.create(user, EventType.UPDATE);
		IVault vault = createVault(user, ensureVaultsInitialized(user), vaultName);
		if (!hasDefaultVault(user)) {
			user.setDefaultVaultId(vault.getId());
		}
		userRepository.update(userId, user);
		IPersistenceEventListener<IVault> vaultListener = listenerFactory.create(vault, EventType.CREATE);
		vaultListener.onEvent(vaultListener.createEvent());
		userListener.onUpdate(user);
		return vault.getId();
	}

	@NotNull
	public IVault resolveOwnedVault(@NotNull String userId, @Nullable String vaultReference) {
		if (vaultReference == null || vaultReference.trim().isEmpty()) {
			return getDefaultVault(userId);
		}
		String normalizedReference = requireText(vaultReference, "Vault reference");
		for (IVault vault : getAllVaults(userId)) {
			if (normalizedReference.equals(vault.getId()) || normalizedReference.equalsIgnoreCase(vault.getName())) {
				return getOwnedVault(userId, vault.getId());
			}
		}
		throw new EntityNotFoundException("The vault with reference " + normalizedReference + " does not exist");
	}

	@NotNull
	public String resolveOwnedVaultId(@NotNull String userId, @Nullable String vaultReference) {
		return resolveOwnedVault(userId, vaultReference).getId();
	}

	@NotNull
	public IVault getDefaultVault(@NotNull String userId) {
		IUser user = getUser(userId);
		String defaultVaultId = user.getDefaultVaultId();
		if (defaultVaultId == null || defaultVaultId.trim().isEmpty()) {
			throw new EntityNotFoundException("The user with id " + userId + " does not have a default vault");
		}
		IVault vault = vaultRepository.findById(defaultVaultId);
		if (vault == null) {
			throw new EntityNotFoundException("The default vault with id " + defaultVaultId + " does not exist");
		}
		return vault;
	}

	@NotNull
	private IVault getOwnedVault(@NotNull String userId, @NotNull String vaultId) {
		requireText(userId, "User id");
		IVault vault = getVault(vaultId);
		if (!userId.equals(vault.getUserId())) {
			throw new UnauthorizedSessionException(
					"The vault with id " + vaultId + " does not belong to user " + userId);
		}
		return vault;
	}

	@NotNull
	private IVault getVault(@NotNull String vaultId) {
		String normalizedVaultId = requireText(vaultId, "Vault id");
		IVault vault = vaultRepository.findById(normalizedVaultId);
		if (vault == null) {
			throw new EntityNotFoundException("The vault with id " + normalizedVaultId + " does not exist");
		}
		return vault;
	}

	@NotNull
	private IVault createVault(@NotNull IUser user) {
		return createVault(user, ensureVaultsInitialized(user), "Vault " + (ensureVaultsInitialized(user).size() + 1));
	}

	@NotNull
	private IVault createVault(@NotNull IUser user, @NotNull List<IVault> vaults, @NotNull String vaultName) {
		String vaultId = UUID.randomUUID().toString();
		IVault vault = PlatformEntityProvider.getEntityProvider().getVault();
		vault.setId(vaultId);
		vault.setName(requireText(vaultName, "Vault name"));
		vault.setUserId(user.getId());
		vault.metadata().setStatus(Status.ACTIVE);
		LocalDateTime currentTime = LocalDateTime.now();
		vault.metadata().setCreatedAt(currentTime);
		vault.metadata().setUpdatedAt(currentTime);
		vault.metadata().setLastAccessedAt(currentTime);
		vault.setEncryptedBlob(encryptPayload(new VaultPayload(), user.getId()));
		vaultRepository.save(vault);
		vaults.add(vault);
		return vault;
	}

	@NotNull
	private String encryptPayload(@NotNull VaultPayload payload, @NotNull String userId) {
		try {
			String rawPayload = objectMapper.writeValueAsString(payload);
			return encryptionService.encrypt(rawPayload, userId);
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to encrypt vault payload for user " + userId, ex);
		}
	}

	@NotNull
	private String requireText(@Nullable String value, @NotNull String fieldName) {
		if (value == null) {
			throw new IllegalArgumentException(fieldName + " cannot be null");
		}
		String normalizedValue = value.trim();
		if (normalizedValue.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " cannot be blank");
		}
		return normalizedValue;
	}

	@NotNull
	private List<IVault> ensureVaultsInitialized(@NotNull IUser user) {
		List<IVault> vaults = user.getVaults();
		if (vaults == null) {
			vaults = new ArrayList<>();
			user.setVaults(vaults);
		}
		return vaults;
	}

	private boolean hasDefaultVault(@NotNull IUser user) {
		String defaultVaultId = user.getDefaultVaultId();
		return defaultVaultId != null && !defaultVaultId.trim().isEmpty();
	}

	private void persistUserIfPresent(@NotNull IUser user) {
		if (userRepository.findById(user.getId()) != null) {
			userRepository.update(user.getId(), user);
		}
	}

	@NotNull
	private IUser getUser(@NotNull String userId) {
		IUser user = userRepository.findById(userId);
		if (user == null) {
			throw new UserNotFoundException("The user with id " + userId + " does not exist");
		}
		return user;
	}
}
