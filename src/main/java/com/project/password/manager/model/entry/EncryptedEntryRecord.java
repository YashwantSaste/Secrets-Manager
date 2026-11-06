package com.project.password.manager.model.entry;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.model.IEntry;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncryptedEntryRecord implements IEntity {

	private String id;
	private String vaultId;
	private String encryptedPayload;
	private long createdAtEpochMs;
	private long updatedAtEpochMs;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getVaultId() {
		return vaultId;
	} 

	public void setVaultId(String vaultId) {
		this.vaultId = vaultId;
	}

	public String getEncryptedPayload() {
		return encryptedPayload;
	}

	public void setEncryptedPayload(String encryptedPayload) {
		this.encryptedPayload = encryptedPayload;
	}

	public long getCreatedAtEpochMs() {
		return createdAtEpochMs;
	}

	public void setCreatedAtEpochMs(long createdAtEpochMs) {
		this.createdAtEpochMs = createdAtEpochMs;
	}

	public long getUpdatedAtEpochMs() {
		return updatedAtEpochMs;
	}

	public void setUpdatedAtEpochMs(long updatedAtEpochMs) {
		this.updatedAtEpochMs = updatedAtEpochMs;
	}

	@Override
	@NotNull
	public Class<?> entityClassType() {
		return IEntry.class;
	}

}