package com.project.password.manager.model.database.sql;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.model.IEntity;
import com.project.password.manager.model.IEntry;
import com.project.password.manager.model.entry.EncryptedEntryRecord;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Entries")
public class JpaEncryptedEntryRecord implements IEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private String id;

	@Column(nullable = false)
	private String vaultId;

	@Column(nullable = false, length = 8192)
	private String encryptedPayload;

	@Column(nullable = false)
	private long createdAtEpochMs;

	@Column(nullable = false)
	private long updatedAtEpochMs;

	protected JpaEncryptedEntryRecord() {
		// for hibernate
	}

	@NotNull
	public static JpaEncryptedEntryRecord from(@NotNull EncryptedEntryRecord record) {
		JpaEncryptedEntryRecord entity = new JpaEncryptedEntryRecord();
		entity.id = record.getId();
		entity.vaultId = record.getVaultId();
		entity.encryptedPayload = record.getEncryptedPayload();
		entity.createdAtEpochMs = record.getCreatedAtEpochMs();
		entity.updatedAtEpochMs = record.getUpdatedAtEpochMs();
		return entity;
	}

	@NotNull
	public EncryptedEntryRecord toDomain() {
		EncryptedEntryRecord record = new EncryptedEntryRecord();
		record.setId(id);
		record.setVaultId(vaultId);
		record.setEncryptedPayload(encryptedPayload);
		record.setCreatedAtEpochMs(createdAtEpochMs);
		record.setUpdatedAtEpochMs(updatedAtEpochMs);
		return record;
	}

	@Override
	@NotNull
	public Class<?> entityClassType() {
		return IEntry.class;
	}
}