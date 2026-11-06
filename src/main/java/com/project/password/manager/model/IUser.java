package com.project.password.manager.model;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface IUser extends HasMetadata {

	@Override
	@NotNull
	default Class<?> entityClassType() {
		return IUser.class;
	}

	@NotNull
	String getId();

	@NotNull
	String getName();

	@NotNull
	String getAuthVerifier();

	@NotNull
	String getKeySalt();

	@NotNull
	String getDefaultVaultId();

	@NotNull
	List<IVault> getVaults();

	@NotNull
	List<UserRole> getRoles();

	void setId(@NotNull String id);

	void setName(@NotNull String name);

	void setAuthVerifier(@NotNull String authVerifier);

	void setKeySalt(@NotNull String keySalt);

	void setDefaultVaultId(@NotNull String defaultVaultId);

	void setVaults(@NotNull List<IVault> vaults);

	void setRoles(@NotNull List<UserRole> roles);

}
