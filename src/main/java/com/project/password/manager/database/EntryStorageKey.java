package com.project.password.manager.database;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EntryStorageKey {

	@NotNull
	private final String vaultId;
	@NotNull
	private final String entryId;

	@Inject
	public EntryStorageKey(@NotNull String vaultId, @NotNull String entryId) {
		this.vaultId = vaultId;
		this.entryId = entryId;
	}

	@NotNull
	public String vaultId() {
		return vaultId;
	}

	@NotNull
	public String entryId() {
		return entryId;
	}

}