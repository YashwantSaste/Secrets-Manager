package com.project.password.manager.database.postgres.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.database.EntryDataRepository;
import com.project.password.manager.database.EntryStorageKey;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.database.sql.JpaEncryptedEntryRecord;
import com.project.password.manager.model.entry.EncryptedEntryRecord;

public class HibernateEntryRepository implements EntryDataRepository {

	@NotNull
	private final SessionFactory sessionFactory;
	@NotNull
	private final ITransactionLogger transactionLogger;

	public HibernateEntryRepository(@NotNull SessionFactory sessionFactory, @NotNull ITransactionLogger transactionLogger) {
		this.sessionFactory = sessionFactory;
		this.transactionLogger = transactionLogger;
	}

	@Override
	public void save(@NotNull EncryptedEntryRecord entity) {
		executeTransaction("save", entity.getId(), session -> session.persist(JpaEncryptedEntryRecord.from(entity)));
	}

	@Override
	@Nullable
	public EncryptedEntryRecord findById(@NotNull EntryStorageKey key) {
		try (Session session = sessionFactory.openSession()) {
			JpaEncryptedEntryRecord record = session.createQuery(
					"from JpaEncryptedEntryRecord where vaultId = :vaultId and id = :entryId",
					JpaEncryptedEntryRecord.class)
				.setParameter("vaultId", key.vaultId())
				.setParameter("entryId", key.entryId())
				.uniqueResult();
			EncryptedEntryRecord result = record == null ? null : record.toDomain();
			logRepositoryOperation("findById", key.entryId(), result == null ? "MISS" : "SUCCESS", null);
			return result;
		}
	}

	@Override
	@NotNull
	public List<EncryptedEntryRecord> findAll() {
		try (Session session = sessionFactory.openSession()) {
			List<EncryptedEntryRecord> records = session.createQuery("from JpaEncryptedEntryRecord", JpaEncryptedEntryRecord.class)
				.getResultList()
				.stream()
				.map(JpaEncryptedEntryRecord::toDomain)
				.toList();
			logRepositoryOperation("findAll", null, "SUCCESS", "count=" + records.size());
			return records;
		}
	}

	@Override
	@NotNull
	public List<EncryptedEntryRecord> findByVaultId(@NotNull String vaultId) {
		try (Session session = sessionFactory.openSession()) {
			List<EncryptedEntryRecord> records = session.createQuery("from JpaEncryptedEntryRecord where vaultId = :vaultId",
					JpaEncryptedEntryRecord.class)
				.setParameter("vaultId", vaultId)
				.getResultList()
				.stream()
				.map(JpaEncryptedEntryRecord::toDomain)
				.toList();
			logRepositoryOperation("findByVaultId", vaultId, "SUCCESS", "count=" + records.size());
			return records;
		}
	}

	@Override
	public void delete(@NotNull EntryStorageKey key) {
		executeTransaction("delete", key.entryId(), session -> {
			JpaEncryptedEntryRecord record = session.createQuery(
					"from JpaEncryptedEntryRecord where vaultId = :vaultId and id = :entryId",
					JpaEncryptedEntryRecord.class)
				.setParameter("vaultId", key.vaultId())
				.setParameter("entryId", key.entryId())
				.uniqueResult();
			if (record != null) {
				session.remove(record);
			}
		});
	}

	@Override
	public void update(@NotNull EntryStorageKey key, @NotNull EncryptedEntryRecord entity) {
		executeTransaction("update", entity.getId(), session -> session.merge(JpaEncryptedEntryRecord.from(entity)));
	}

	private void executeTransaction(@NotNull String operation, @Nullable String entityId,
			@NotNull DatabaseTransaction action) {
		Transaction tx = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			action.accept(session);
			tx.commit();
			logRepositoryOperation(operation, entityId, "SUCCESS", null);
		} catch (Exception ex) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logRepositoryOperation(operation, entityId, "FAILURE", ex.getMessage());
			throw new RuntimeException(ex);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@FunctionalInterface
	private interface DatabaseTransaction {
		void accept(@NotNull Session session);
	}

	private void logRepositoryOperation(@NotNull String operation, @Nullable String entityId, @NotNull String status,
			@Nullable String details) {
		transactionLogger.logRepositoryOperation(true, getClass().getSimpleName(), operation,
				EncryptedEntryRecord.class.getSimpleName(), entityId, status, details);
	}
}