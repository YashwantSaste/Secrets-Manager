package com.project.password.manager.guice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;
import com.project.password.manager.argon.Argon2Encoder;
import com.project.password.manager.auth.token.JwtSessionTokenStrategy;
import com.project.password.manager.auth.token.OAuth2SessionTokenStrategy;
import com.project.password.manager.auth.token.SessionTokenStrategy;
import com.project.password.manager.auth.token.SessionTokenStrategyRegistry;
import com.project.password.manager.cli.commands.auth.LoginCommand;
import com.project.password.manager.cli.commands.auth.LogoutCommand;
import com.project.password.manager.cli.commands.auth.OAuth2LoginCommand;
import com.project.password.manager.cli.commands.auth.PingCommand;
import com.project.password.manager.cli.commands.auth.SignupCommand;
import com.project.password.manager.cli.commands.auth.WhoAmICommand;
import com.project.password.manager.cli.commands.config.ConfigGetCommand;
import com.project.password.manager.cli.commands.config.ConfigListCommand;
import com.project.password.manager.cli.commands.config.ConfigSetCommand;
import com.project.password.manager.cli.commands.entry.EntryCreateCommand;
import com.project.password.manager.cli.commands.entry.EntryDeleteCommand;
import com.project.password.manager.cli.commands.entry.EntryGetCommand;
import com.project.password.manager.cli.commands.entry.EntryListCommand;
import com.project.password.manager.cli.commands.entry.EntrySearchCommand;
import com.project.password.manager.cli.commands.entry.EntryUpdateCommand;
import com.project.password.manager.cli.commands.theme.ThemeListCommand;
import com.project.password.manager.cli.commands.theme.ThemePreviewCommand;
import com.project.password.manager.cli.commands.theme.ThemeSetCommand;
import com.project.password.manager.cli.commands.user.UserRoleGrantCommand;
import com.project.password.manager.cli.commands.user.UserRoleListCommand;
import com.project.password.manager.cli.commands.user.UserRoleRevokeCommand;
import com.project.password.manager.cli.commands.vault.VaultCreateCommand;
import com.project.password.manager.cli.commands.vault.VaultDefaultCommand;
import com.project.password.manager.cli.commands.vault.VaultListCommand;
import com.project.password.manager.cli.handlers.auth.LoginCommandHandler;
import com.project.password.manager.cli.handlers.auth.LogoutCommandHandler;
import com.project.password.manager.cli.handlers.auth.OAuth2LoginCommandHandler;
import com.project.password.manager.cli.handlers.auth.PingCommandHandler;
import com.project.password.manager.cli.handlers.auth.SignupCommandHandler;
import com.project.password.manager.cli.handlers.auth.WhoAmICommandHandler;
import com.project.password.manager.cli.handlers.config.ConfigGetCommandHandler;
import com.project.password.manager.cli.handlers.config.ConfigListCommandHandler;
import com.project.password.manager.cli.handlers.config.ConfigSetCommandHandler;
import com.project.password.manager.cli.handlers.entry.EntryCreateCommandHandler;
import com.project.password.manager.cli.handlers.entry.EntryDeleteCommandHandler;
import com.project.password.manager.cli.handlers.entry.EntryGetCommandHandler;
import com.project.password.manager.cli.handlers.entry.EntryListCommandHandler;
import com.project.password.manager.cli.handlers.entry.EntrySearchCommandHandler;
import com.project.password.manager.cli.handlers.entry.EntryUpdateCommandHandler;
import com.project.password.manager.cli.handlers.theme.ThemeListCommandHandler;
import com.project.password.manager.cli.handlers.theme.ThemePreviewCommandHandler;
import com.project.password.manager.cli.handlers.theme.ThemeSetCommandHandler;
import com.project.password.manager.cli.handlers.user.UserRoleGrantCommandHandler;
import com.project.password.manager.cli.handlers.user.UserRoleListCommandHandler;
import com.project.password.manager.cli.handlers.user.UserRoleRevokeCommandHandler;
import com.project.password.manager.cli.handlers.vault.VaultCreateCommandHandler;
import com.project.password.manager.cli.handlers.vault.VaultDefaultCommandHandler;
import com.project.password.manager.cli.handlers.vault.VaultListCommandHandler;
import com.project.password.manager.cli.runtime.CliOutput;
import com.project.password.manager.cli.runtime.CliSession;
import com.project.password.manager.cli.runtime.CommandHandlerInvoker;
import com.project.password.manager.cli.runtime.CommandHandlerRegistry;
import com.project.password.manager.cli.runtime.ConsoleCliOutput;
import com.project.password.manager.configuration.AuthenticationType;
import com.project.password.manager.configuration.IAuthenticationConfiguration;
import com.project.password.manager.configuration.IConfiguration;
import com.project.password.manager.configuration.IDatabaseConfiguration;
import com.project.password.manager.configuration.IJwtConfiguration;
import com.project.password.manager.configuration.IOAuth2Configuration;
import com.project.password.manager.configuration.application.Configuration;
import com.project.password.manager.configuration.application.OAuth2Configuration;
import com.project.password.manager.configuration.application.PropertiesReader;
import com.project.password.manager.database.DataRepository;
import com.project.password.manager.database.DataRepositoryFactory;
import com.project.password.manager.database.EntryDataRepository;
import com.project.password.manager.database.EntryStorageKey;
import com.project.password.manager.encryption.AesGcmEncryptionService;
import com.project.password.manager.encryption.IEncryptionService;
import com.project.password.manager.event.EntityChangeDetector;
import com.project.password.manager.event.EntityEventFactory;
import com.project.password.manager.event.EntityEventSupport;
import com.project.password.manager.event.EntitySnapshotter;
import com.project.password.manager.event.EventDispatcher;
import com.project.password.manager.event.EventLogger;
import com.project.password.manager.event.IEntityEventSupport;
import com.project.password.manager.event.IEventPublisher;
import com.project.password.manager.event.listener.EventLoggingListener;
import com.project.password.manager.event.listener.IEventListener;
import com.project.password.manager.event.listener.IPersistenceEventListener;
import com.project.password.manager.event.listener.IPersistenceListenerSubscriberManager;
import com.project.password.manager.event.listener.PersistenceListener;
import com.project.password.manager.event.listener.PersistenceListenerSubscriberManager;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.logging.WorkspaceTransactionLogger;
import com.project.password.manager.middleware.RequireAuthorization;
import com.project.password.manager.middleware.TokenAuthorizationInterceptor;
import com.project.password.manager.model.IMetadata;
import com.project.password.manager.model.IToken;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IVault;
import com.project.password.manager.model.database.file.storage.Metadata;
import com.project.password.manager.model.database.file.storage.Token;
import com.project.password.manager.model.database.file.storage.User;
import com.project.password.manager.model.database.file.storage.Vault;
import com.project.password.manager.model.database.nosql.UserDocument;
import com.project.password.manager.model.database.sql.JpaToken;
import com.project.password.manager.model.database.sql.JpaUser;
import com.project.password.manager.model.database.sql.JpaVault;
import com.project.password.manager.model.entry.EncryptedEntryRecord;
import com.project.password.manager.service.AuthService;
import com.project.password.manager.service.EntryService;
import com.project.password.manager.service.OAuth2LoginService;
import com.project.password.manager.service.TokenService;
import com.project.password.manager.service.UserService;
import com.project.password.manager.service.VaultService;
import com.project.password.manager.util.ModelObjectMapperFactory;
import com.project.password.manager.workspace.IWorkspace;
import com.project.password.manager.workspace.IWorkspaceLocator;
import com.project.password.manager.workspace.Workspace;
import com.project.password.manager.workspace.WorkspaceLocator;

public class GuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        IConfiguration configuration = Configuration.getInstance();
        TokenAuthorizationInterceptor authorizationInterceptor = new TokenAuthorizationInterceptor();
        bind(IConfiguration.class).toInstance(configuration);
        bind(IWorkspace.class).to(Workspace.class).in(Singleton.class);
        bind(IWorkspaceLocator.class).to(WorkspaceLocator.class).in(Singleton.class);
        bind(IPersistenceEventListener.class).to(PersistenceListener.class);
        bind(IPersistenceListenerSubscriberManager.class).to(PersistenceListenerSubscriberManager.class).in(Singleton.class);
        bind(CliSession.class).in(Singleton.class);
        bind(CliOutput.class).to(ConsoleCliOutput.class).in(Singleton.class);
        requestInjection(authorizationInterceptor);
        bindInterceptor(Matchers.any(), authorizationMethodMatcher(), authorizationInterceptor);
        if (!configuration.databaseConfiguration().databaseEnabled()) {
            bind(IUser.class).to(User.class);
            bind(IVault.class).to(Vault.class);
            bind(IMetadata.class).to(Metadata.class);
            bind(IToken.class).to(Token.class);
        } else {
            switch (configuration.databaseConfiguration().type()) {
                case IDatabaseConfiguration.DATABASE_TYPE_SQL: {
                    bind(IUser.class).to(JpaUser.class);
                    bind(IVault.class).to(JpaVault.class);
                    bind(IToken.class).to(JpaToken.class);
                    bind(IMetadata.class).to(Metadata.class);
                    break;
                }
                case IDatabaseConfiguration.DATABASE_TYPE_NO_SQL: {
                    bind(IUser.class).to(UserDocument.class);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Could not bind entities: Error: Unsupported Database Type");
            }
        }

    }

    @NotNull
    private Matcher<Method> authorizationMethodMatcher() {
        return new Matcher<Method>() {
            @Override
            public boolean matches(Method method) {
                return !method.isSynthetic() && method.isAnnotationPresent(RequireAuthorization.class);
            }
        };
    }

    @Provides
    @Singleton
    IOAuth2Configuration provideOAuth2Configuration() {
        return new OAuth2Configuration(PropertiesReader.getInstance());
    }

    @Provides
    @Singleton
    Argon2Encoder provideArgon2Encoder(IConfiguration configuration) {
        return new Argon2Encoder(configuration.argon2Configuration());
    }

    @Provides
    @Singleton
    DataRepository<IUser, String> provideUserRepository(IConfiguration configuration) {
        return new DataRepositoryFactory(configuration).getRepository(IUser.class, String.class);
    }

    @Provides
    @Singleton
    DataRepository<IToken, String> provideTokenRepository(IConfiguration configuration) {
        return new DataRepositoryFactory(configuration).getRepository(IToken.class, String.class);
    }

    @Provides
    @Singleton
    DataRepository<IVault, String> provideVaultRepository(@NotNull IConfiguration configuration) {
        return new DataRepositoryFactory(configuration).getRepository(IVault.class, String.class);
    }

    @Provides
    @Singleton
    EntryDataRepository provideEntryRepository(IConfiguration configuration) {
        return (EntryDataRepository) new DataRepositoryFactory(configuration).getRepository(EncryptedEntryRecord.class, EntryStorageKey.class);
    }

    @Provides
    @Singleton
    ITransactionLogger provideTransactionLogger(IConfiguration configuration) {
        return new WorkspaceTransactionLogger(configuration.appConfiguration(), Workspace.getInstance().getRoot());
    }

    @Provides
    @Singleton
    UserService provideUserService(DataRepository<IUser, String> userRepository) {
        return new UserService(userRepository);
    }

    @Provides
    @Singleton
    TokenService provideTokenService(IConfiguration configuration) {
        return new TokenService(provideSessionTokenStrategyRegistry(configuration));
    }

    @Provides
    @Singleton
    SessionTokenStrategyRegistry provideSessionTokenStrategyRegistry(IConfiguration configuration) {
        List<SessionTokenStrategy> strategies = new ArrayList<>();
        for (IAuthenticationConfiguration authenticationConfiguration : List.of(configuration.jwtConfiguration(),
                configuration.oauth2Configuration())) {
            SessionTokenStrategy strategy = createSessionTokenStrategy(authenticationConfiguration);
            if (strategy != null) {
                strategies.add(strategy);
            }
        }
        return new SessionTokenStrategyRegistry(strategies);
    }

    @Nullable
    private SessionTokenStrategy createSessionTokenStrategy(@NotNull IAuthenticationConfiguration authenticationConfiguration) {
        try {
            switch (authenticationConfiguration.type()) {
                case OAUTH2:
                    return new OAuth2SessionTokenStrategy();
                case JWT:
                    return new JwtSessionTokenStrategy((IJwtConfiguration) authenticationConfiguration);
                default:
                    return null;
            }
        } catch (RuntimeException exception) {
            if (authenticationConfiguration.type() == AuthenticationType.JWT) {
                return null;
            }
            throw exception;
        }
    }

    @Provides
    @Singleton
    EventLogger provideEventLogger(ITransactionLogger transactionLogger) {
        return new EventLogger(transactionLogger);
    }

    @Provides
    @Singleton
    IEventListener provideEventLoggingListener(EventLogger eventLogger) {
        return new EventLoggingListener(eventLogger);
    }

    @Provides
    @Singleton
    IEventPublisher provideEventPublisher(IEventListener eventListener) {
        return new EventDispatcher(List.of(eventListener));
    }

    @Provides
    @Singleton
    IEntityEventSupport provideEntityEventSupport(IEventPublisher eventPublisher) {
        return new EntityEventSupport(new EntitySnapshotter(ModelObjectMapperFactory.create()),
                new EntityEventFactory(new EntityChangeDetector(ModelObjectMapperFactory.create())), eventPublisher);
    }

    @Provides
    @Singleton
    IEncryptionService provideEncryptionService(UserService userService) {
        return new AesGcmEncryptionService(userService);
    }

    @Provides
    @Singleton
    VaultService provideVaultService(DataRepository<IUser, String> userRepository, DataRepository<IVault, String> vaultRepository, IEncryptionService encryptionService, IEntityEventSupport eventSupport) {
        return new VaultService(userRepository, vaultRepository, encryptionService, ModelObjectMapperFactory.create(),
                eventSupport);
    }

    @Provides
    @Singleton
    EntryService provideEntryService(EntryDataRepository entryRepository, DataRepository<IVault, String> vaultRepository, IEncryptionService encryptionService, IEntityEventSupport eventSupport) {
        return new EntryService(entryRepository, vaultRepository, encryptionService, eventSupport);
    }

    @Provides
    @Singleton
    VaultService provideVaultService(DataRepository<IUser, String> userRepository, DataRepository<IVault, String> vaultRepository, IEncryptionService encryptionService) {
        return new VaultService(userRepository, vaultRepository, encryptionService, ModelObjectMapperFactory.create());
    }

    @Provides
    @Singleton
    EntryService provideEntryService(EntryDataRepository entryRepository, DataRepository<IVault, String> vaultRepository, IEncryptionService encryptionService) {
        return new EntryService(entryRepository, vaultRepository, encryptionService);
    }

    @Provides
    @Singleton
    AuthService provideAuthService(TokenService tokenService, UserService userService, Argon2Encoder argon2Encoder, VaultService vaultService) {
        return new AuthService(tokenService, userService, argon2Encoder, vaultService);
    }

    @Provides
    @Singleton
    OAuth2LoginService provideOAuth2LoginService(IOAuth2Configuration oauth2Configuration, AuthService authService, TokenService tokenService) {
        return new OAuth2LoginService(oauth2Configuration, authService, tokenService);
    }

    @Provides
    @Singleton
    CommandHandlerInvoker provideCommandHandlerInvoker() {
        return new CommandHandlerInvoker();
    }

    @Provides
    @Singleton
    CommandHandlerRegistry provideCommandHandlerRegistry() {
        return new CommandHandlerRegistry()
                .register(ConfigListCommand.class, ConfigListCommandHandler.class)
                .register(ConfigGetCommand.class, ConfigGetCommandHandler.class)
                .register(ConfigSetCommand.class, ConfigSetCommandHandler.class)
                .register(LoginCommand.class, LoginCommandHandler.class)
                .register(OAuth2LoginCommand.class, OAuth2LoginCommandHandler.class)
                .register(SignupCommand.class, SignupCommandHandler.class)
                .register(LogoutCommand.class, LogoutCommandHandler.class)
                .register(WhoAmICommand.class, WhoAmICommandHandler.class)
                .register(PingCommand.class, PingCommandHandler.class)
                .register(UserRoleListCommand.class, UserRoleListCommandHandler.class)
                .register(UserRoleGrantCommand.class, UserRoleGrantCommandHandler.class)
                .register(UserRoleRevokeCommand.class, UserRoleRevokeCommandHandler.class)
                .register(ThemeListCommand.class, ThemeListCommandHandler.class)
                .register(ThemePreviewCommand.class, ThemePreviewCommandHandler.class)
                .register(ThemeSetCommand.class, ThemeSetCommandHandler.class)
                .register(VaultListCommand.class, VaultListCommandHandler.class)
                .register(VaultCreateCommand.class, VaultCreateCommandHandler.class)
                .register(VaultDefaultCommand.class, VaultDefaultCommandHandler.class)
                .register(EntryListCommand.class, EntryListCommandHandler.class)
                .register(EntryGetCommand.class, EntryGetCommandHandler.class)
                .register(EntryCreateCommand.class, EntryCreateCommandHandler.class)
                .register(EntryUpdateCommand.class, EntryUpdateCommandHandler.class)
                .register(EntryDeleteCommand.class, EntryDeleteCommandHandler.class)
                .register(EntrySearchCommand.class, EntrySearchCommandHandler.class);
    }

}
