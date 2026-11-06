package com.project.password.manager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.project.password.manager.database.DataRepository;
import com.project.password.manager.encryption.IEncryptionService;
import com.project.password.manager.event.EventType;
import com.project.password.manager.event.listener.IPersistenceEventListener;
import com.project.password.manager.event.listener.PersistenceListenerFactory;
import com.project.password.manager.exceptions.EntityNotFoundException;
import com.project.password.manager.exceptions.UserNotFoundException;
import com.project.password.manager.guice.Platform;
import com.project.password.manager.model.ITeam;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IVault;
import com.project.password.manager.model.Status;
import com.project.password.manager.model.VaultPayload;
import com.project.password.manager.model.VaultScope;
import com.project.password.manager.util.ModelObjectMapperFactory;

import jakarta.validation.constraints.NotNull;

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
	private final VaultAccessService vaultAccessService;
	@NotNull
	private final TeamService teamService;
	private final PersistenceListenerFactory listenerFactory;

	@Inject
	public VaultService(@NotNull DataRepository<IUser, String> userRepository,
			@NotNull DataRepository<IVault, String> vaultRepository, @NotNull IEncryptionService encryptionService,
			@NotNull ObjectMapper objectMapper, @NotNull TeamService teamService,
			@NotNull PersistenceListenerFactory listenerFactory) {
		this.userRepository = userRepository;
		this.vaultRepository = vaultRepository;
		this.encryptionService = encryptionService;
		this.objectMapper = objectMapper;
		this.teamService = teamService;
		this.vaultAccessService = new VaultAccessService(vaultRepository, teamService);
		this.listenerFactory = listenerFactory;
	}

	@NotNull
	public IVault createDefaultVault(@NotNull IUser user) {
		IPersistenceEventListener<IUser> userListener = listenerFactory.create(user, EventType.UPDATE);
		List<IVault> vaults = ensureVaultsInitialized(user);
		if (!vaults.isEmpty() || hasDefaultVault(user)) {
			throw new UnsupportedOperationException("A user can have only one default vault.");
		}
		IVault vault = createScopedVault(VaultScope.USER, user.getId(), "Default");
		vaults.add(vault);
		user.setDefaultVaultId(vault.getId());
		persistUserIfPresent(user);
		userListener.onUpdate(user);
		return vault;
	}

	@NotNull
	public List<IVault> getAllVaults(@NotNull String userId) {
		return vaultAccessService.findVaultsAccessibleToUser(userId);
	}

	@NotNull
	public String createVaultForUser(@NotNull String userId, @NotNull String vaultName) {
		IUser user = getUser(userId);
		IPersistenceEventListener<IUser> userListener = listenerFactory.create(user, EventType.UPDATE);
		IVault vault = createScopedVault(VaultScope.USER, user.getId(), vaultName);
		ensureVaultsInitialized(user).add(vault);
		if (!hasDefaultVault(user)) {
			user.setDefaultVaultId(vault.getId());
		}
		userRepository.update(userId, user);
		userListener.onUpdate(user);
		return vault.getId();
	}

	@NotNull
	public String createTeamVault(@NotNull String actorUserId, @NotNull String teamId, @NotNull String vaultName) {
		ITeam team = teamService.getTeam(teamId);
		if (!teamService.isUserOwnerOfTeam(teamId, actorUserId)) {
			throw new IllegalArgumentException("Only team owners can create team vaults.");
		}
		IVault vault = createScopedVault(VaultScope.TEAM, team.getId(), vaultName);
		if (team.getDefaultVaultId().isBlank()) {
			team.setDefaultVaultId(vault.getId());
			teamService.saveOrUpdateTeam(team);
		}
		return vault.getId();
	}

	@NotNull
	public List<IVault> getTeamVaults(@NotNull String userId, @NotNull String teamId) {
		ITeam team = teamService.requireTeamAccessibleToUser(teamId, userId);
		String normalizedTeamId = team.getId().trim();
		List<IVault> teamVaults = new ArrayList<>();
		for (IVault vault : vaultRepository.findAll()) {
			if (vault.getScope() == VaultScope.TEAM && normalizedTeamId.equals(vault.getScopeId().trim())) {
				teamVaults.add(vault);
			}
		}
		return teamVaults;
	}

	@NotNull
	public IVault resolveVaultAccessibleToUser(@NotNull String userId, @Nullable String vaultReference) {
		if (vaultReference == null || vaultReference.trim().isEmpty()) {
			return getDefaultVault(userId);
		}
		String normalizedReference = requireText(vaultReference, "Vault reference");
		for (IVault vault : getAllVaults(userId)) {
			if (normalizedReference.equals(vault.getId()) || normalizedReference.equalsIgnoreCase(vault.getName())) {
				return vaultAccessService.requireUserAccessibleVault(userId, vault.getId());
			}
		}
		throw new EntityNotFoundException("The vault with reference " + normalizedReference + " does not exist");
	}

	@NotNull
	public String resolveVaultIdAccessibleToUser(@NotNull String userId, @Nullable String vaultReference) {
		return resolveVaultAccessibleToUser(userId, vaultReference).getId();
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
	public IVault getVault(@NotNull String vaultId) {
		String normalizedVaultId = requireText(vaultId, "Vault id");
		IVault vault = vaultRepository.findById(normalizedVaultId);
		if (vault == null) {
			throw new EntityNotFoundException("The vault with id " + normalizedVaultId + " does not exist");
		}
		return vault;
	}

	@NotNull
	private IVault createVault(@NotNull IUser user) {
		return createScopedVault(VaultScope.USER, user.getId(), "Vault " + (ensureVaultsInitialized(user).size() + 1));
	}

	@NotNull
	private IVault createScopedVault(@NotNull VaultScope scope, @NotNull String scopeId, @NotNull String vaultName) {
		String vaultId = UUID.randomUUID().toString();
		IVault vault = Platform.getPlatformContext().getVault();
		vault.setId(vaultId);
		vault.setName(requireText(vaultName, "Vault name"));
		vault.setScope(scope);
		vault.setScopeId(scopeId);
		vault.metadata().setStatus(Status.ACTIVE);
		LocalDateTime currentTime = LocalDateTime.now();
		vault.metadata().setCreatedAt(currentTime);
		vault.metadata().setUpdatedAt(currentTime);
		vault.metadata().setLastAccessedAt(currentTime);
		vault.setEncryptedBlob(encryptPayload(vault, new VaultPayload()));
		vaultRepository.save(vault);
		IPersistenceEventListener<IVault> listener = listenerFactory.create(vault, EventType.CREATE);
		listener.onEvent(listener.createEvent());
		return vault;
	}

	@NotNull
	private String encryptPayload(@NotNull IVault vault, @NotNull VaultPayload payload) {
		try {
			String rawPayload = objectMapper.writeValueAsString(payload);
			return encryptionService.encrypt(rawPayload, vault);
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to encrypt vault payload for vault " + vault.getId(), ex);
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
