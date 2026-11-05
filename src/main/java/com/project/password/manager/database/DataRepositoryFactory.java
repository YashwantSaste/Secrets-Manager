package com.project.password.manager.database;

import java.io.File;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import com.project.password.manager.configuration.IDatabaseConfiguration;
import com.project.password.manager.database.file.storage.FileEntryRepository;
import com.project.password.manager.database.file.storage.TokenRepository;
import com.project.password.manager.database.file.storage.UserRepository;
import com.project.password.manager.database.file.storage.VaultRepository;
import com.project.password.manager.database.postgres.hibernate.HibernateBootStrap;
import com.project.password.manager.database.postgres.hibernate.HibernateEntryRepository;
import com.project.password.manager.database.postgres.hibernate.HibernateEntityProvider;
import com.project.password.manager.database.postgres.hibernate.HibernateRepository;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.model.IToken;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IVault;
import com.project.password.manager.model.database.sql.JpaToken;
import com.project.password.manager.model.database.sql.JpaUser;
import com.project.password.manager.model.database.sql.JpaVault;
import com.project.password.manager.util.Logger;
import com.project.password.manager.workspace.Workspace;

public class DataRepositoryFactory {

	private static final Logger log = Logger.getLogger(DataRepositoryFactory.class);
	@NotNull
	private final IDatabaseConfiguration databaseConfiguration;

	public DataRepositoryFactory(@NotNull IDatabaseConfiguration databaseConfiguration) {
		this.databaseConfiguration = databaseConfiguration;
	}

	@NotNull
	public <T extends IEntity, Id> DataRepository<T, Id> getRepository(@NotNull Class<T> entityClass,
			@NotNull Class<Id> idClass) {
		if (databaseConfiguration.databaseEnabled()
				&& IDatabaseConfiguration.DATABASE_TYPE_SQL.equalsIgnoreCase(databaseConfiguration.type())) {
			SessionFactory factory = HibernateBootStrap.init(databaseConfiguration, new HibernateEntityProvider());
			return new HibernateRepository<>(factory, resolveSqlEntityClass(entityClass));
		}
		log.warn("Database is not enabled hence using local file system as storage");
		File workspace = Workspace.getInstance().getRoot();
		if (entityClass.equals(IUser.class)) {
			@SuppressWarnings("unchecked")
			DataRepository<T, Id> repo = (DataRepository<T, Id>) new UserRepository(workspace);
			return repo;
		}
		if(entityClass.equals(IToken.class)) {
			@SuppressWarnings("unchecked")
			DataRepository<T, Id> repo = (DataRepository<T, Id>) new TokenRepository(workspace);
			return repo;
		}
		if (entityClass.equals(IVault.class)) {
			@SuppressWarnings("unchecked")
			DataRepository<T, Id> repo = (DataRepository<T, Id>) new VaultRepository(workspace);
			return repo;
		}
		throw new IllegalArgumentException("No repository available for entity: " + entityClass.getName());
	}

	@NotNull
	public EntryDataRepository getEntryRepository() {
		if (databaseConfiguration.databaseEnabled()) {
			if (IDatabaseConfiguration.DATABASE_TYPE_SQL.equalsIgnoreCase(databaseConfiguration.type())) {
				SessionFactory factory = HibernateBootStrap.init(databaseConfiguration, new HibernateEntityProvider());
				return new HibernateEntryRepository(factory);
			}
			if (IDatabaseConfiguration.DATABASE_TYPE_NO_SQL.equalsIgnoreCase(databaseConfiguration.type())) {
				throw new UnsupportedOperationException("NoSQL entry repository is not implemented yet");
			}
		}
		log.warn("Database is not enabled hence using local file system as storage");
		return new FileEntryRepository(Workspace.getInstance().getRoot());
	}

	@NotNull
	@SuppressWarnings("unchecked")
	private <T extends IEntity> Class<T> resolveSqlEntityClass(@NotNull Class<T> entityClass) {
		if (entityClass.equals(IUser.class)) {
			return (Class<T>) JpaUser.class;
		}
		if (entityClass.equals(IToken.class)) {
			return (Class<T>) JpaToken.class;
		}
		if (entityClass.equals(IVault.class)) {
			return (Class<T>) JpaVault.class;
		}
		return entityClass;
	}
}
