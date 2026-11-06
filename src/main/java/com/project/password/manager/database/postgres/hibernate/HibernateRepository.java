package com.project.password.manager.database.postgres.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.database.DataRepository;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.util.MetadataListener;

public class HibernateRepository<T extends IEntity, Id> implements DataRepository<T, Id> {

	@NotNull
	private final SessionFactory sessionFactory;
	@NotNull
	private final Class<T> entity;
	@NotNull
	private final ITransactionLogger transactionLogger;

	public HibernateRepository(@NotNull SessionFactory sessionFactory, @NotNull Class<T> entity,
			@NotNull ITransactionLogger transactionLogger) {
		this.sessionFactory = sessionFactory;
		this.entity = entity;
		this.transactionLogger = transactionLogger;
	}

	@Override
	public void save(@NotNull T entity) {
		MetadataListener.beforeCreate(entity);
		executeTransaction("save", resolveEntityId(entity), session -> session.persist(entity));
	}

	@Override
	@Nullable
	public T findById(@NotNull Id id) {
		try (Session session = sessionFactory.openSession()) {
			T persistedEntity = session.get(entity, (java.io.Serializable) id);
			logRepositoryOperation("findById", String.valueOf(id), persistedEntity == null ? "MISS" : "SUCCESS", null);
			return persistedEntity;
		}
	}

	@Override
	@NotNull
	public List<T> findAll() {
		try (Session session = sessionFactory.openSession()) {
			List<T> entities = session.createQuery("from " + entity.getSimpleName(), entity).getResultList();
			logRepositoryOperation("findAll", null, "SUCCESS", "count=" + entities.size());
			return entities;
		}
	}

	@Override
	public void delete(@NotNull Id id) {
		executeTransaction("delete", String.valueOf(id), session -> {
			T persistedEntity = session.get(entity, (java.io.Serializable) id);
			if (persistedEntity != null) {
				session.remove(persistedEntity);
			}
		});
	}

	@Override
	public void update(@NotNull Id id, @NotNull T entity) {
		MetadataListener.beforeUpdate(entity);
		executeTransaction("update", resolveEntityId(entity), session -> session.merge(entity));
	}

	private void executeTransaction(String operation, @Nullable String entityId, DatabaseTransaction<IEntity> action) {
		Transaction tx = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			action.accept(session);
			tx.commit();
			logRepositoryOperation(operation, entityId, "SUCCESS", null);
		} catch (Exception e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
			}
			logRepositoryOperation(operation, entityId, "FAILURE", e.getMessage());
			throw new RuntimeException(e);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	@FunctionalInterface
	private interface DatabaseTransaction<T> {
		void accept(@NotNull Session session);
	}

	private void logRepositoryOperation(@NotNull String operation, @Nullable String entityId, @NotNull String status,
			@Nullable String details) {
		transactionLogger.logRepositoryOperation(true, getClass().getSimpleName(), operation, entity.getSimpleName(),
				entityId, status, details);
	}

	@Nullable
	private String resolveEntityId(@NotNull T entity) {
		try {
			Object id = entity.getClass().getMethod("getId").invoke(entity);
			return id == null ? null : String.valueOf(id);
		} catch (ReflectiveOperationException exception) {
			return null;
		}
	}
}
