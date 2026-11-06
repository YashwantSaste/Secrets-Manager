package com.project.password.manager.database;

import org.hibernate.SessionFactory;
import jakarta.validation.constraints.NotNull;

import com.project.password.manager.configuration.IConfiguration;
import com.project.password.manager.configuration.IDatabaseConfiguration;
import com.project.password.manager.database.file.storage.CustomRoleRepository;
import com.project.password.manager.database.file.storage.FileEntryRepository;
import com.project.password.manager.database.file.storage.TeamRepository;
import com.project.password.manager.database.file.storage.TokenRepository;
import com.project.password.manager.database.file.storage.UserGroupRepository;
import com.project.password.manager.database.file.storage.UserRepository;
import com.project.password.manager.database.file.storage.VaultRepository;
import com.project.password.manager.database.postgres.hibernate.HibernateBootStrap;
import com.project.password.manager.database.postgres.hibernate.HibernateEntityProvider;
import com.project.password.manager.database.postgres.hibernate.HibernateEntryRepository;
import com.project.password.manager.database.postgres.hibernate.HibernateRepository;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.logging.WorkspaceTransactionLogger;
import com.project.password.manager.model.ICustomRole;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.model.ITeam;
import com.project.password.manager.model.IToken;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IUserGroup;
import com.project.password.manager.model.IVault;
import com.project.password.manager.model.database.sql.JpaToken;
import com.project.password.manager.model.database.sql.JpaUser;
import com.project.password.manager.model.database.sql.JpaVault;
import com.project.password.manager.model.entry.EncryptedEntryRecord;
import com.project.password.manager.util.Logger;
import com.project.password.manager.workspace.IWorkspaceLocator;
import com.project.password.manager.workspace.Workspace;

public class DataRepositoryFactory {

    private static final Logger log = Logger.getLogger(DataRepositoryFactory.class);
    private final IDatabaseConfiguration databaseConfiguration;
    private final ITransactionLogger transactionLogger;
    private final IWorkspaceLocator locator;

    public DataRepositoryFactory(@NotNull IConfiguration configuration, @NotNull IWorkspaceLocator locator) {
        this.databaseConfiguration = configuration.databaseConfiguration();
        this.transactionLogger = new WorkspaceTransactionLogger(configuration.appConfiguration(),
                Workspace.getInstance().getRoot());
        this.locator = locator;
    }

    @NotNull
    public <T extends IEntity, Id> DataRepository<T, Id> getRepository(@NotNull Class<T> entityClass,
            @NotNull Class<Id> idClass) {
        if (entityClass.equals(EncryptedEntryRecord.class)) {
            return castRepository(createEntryRepository());
        }
        if (entityClass.equals(ITeam.class)) {
            return castRepository(new TeamRepository(locator, transactionLogger));
        }
        if (entityClass.equals(ICustomRole.class)) {
            return castRepository(new CustomRoleRepository(locator, transactionLogger));
        }
        if (entityClass.equals(IUserGroup.class)) {
            return castRepository(new UserGroupRepository(locator, transactionLogger));
        }
        if (databaseConfiguration.databaseEnabled()
                && IDatabaseConfiguration.DATABASE_TYPE_SQL.equalsIgnoreCase(databaseConfiguration.type())) {
            SessionFactory factory = HibernateBootStrap.init(databaseConfiguration, new HibernateEntityProvider());
            return new HibernateRepository<>(factory, resolveSqlEntityClass(entityClass), transactionLogger);
        }
        log.warn("Database is not enabled hence using local file system as storage");
        if (entityClass.equals(IUser.class)) {
            return castRepository(new UserRepository(locator, transactionLogger));
        }
        if (entityClass.equals(IToken.class)) {
            return castRepository(new TokenRepository(locator, transactionLogger));
        }
        if (entityClass.equals(IVault.class)) {
            return castRepository(new VaultRepository(locator, transactionLogger));
        }
        throw new IllegalArgumentException("No repository available for entity: " + entityClass.getName());
    }

    @SuppressWarnings("unchecked")
    private <T extends IEntity, Id> DataRepository<T, Id> castRepository(DataRepository<?, ?> repository) {
        return (DataRepository<T, Id>) repository;
    }

    @NotNull
    public EntryDataRepository getEntryRepository() {
        return createEntryRepository();
    }

    @NotNull
    private EntryDataRepository createEntryRepository() {
        if (databaseConfiguration.databaseEnabled()) {
            if (IDatabaseConfiguration.DATABASE_TYPE_SQL.equalsIgnoreCase(databaseConfiguration.type())) {
                SessionFactory factory = HibernateBootStrap.init(databaseConfiguration, new HibernateEntityProvider());
                return new HibernateEntryRepository(factory, transactionLogger);
            }
            if (IDatabaseConfiguration.DATABASE_TYPE_NO_SQL.equalsIgnoreCase(databaseConfiguration.type())) {
                throw new UnsupportedOperationException("NoSQL entry repository is not implemented yet");
            }
        }
        log.warn("Database is not enabled hence using local file system as storage");
        return new FileEntryRepository(locator, transactionLogger);
    }

    @SuppressWarnings("unchecked")
    @NotNull
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
