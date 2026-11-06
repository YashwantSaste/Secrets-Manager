package com.project.password.manager.configuration.application;

import jakarta.validation.constraints.NotNull;

import com.project.password.manager.configuration.IAppConfiguration;

public class ApplicationConfiguration implements IAppConfiguration {

    @NotNull
    private final PropertiesReader reader;

    public ApplicationConfiguration(@NotNull PropertiesReader reader) {
        this.reader = reader;
    }

    @Override
    @NotNull
    public String name() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_APP_NAME);
    }

    @Override
    @NotNull
    public String version() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_APP_VERSION);
    }

    @Override
    public boolean transactionLoggingEnabled() {
        return reader.readPropertyAsBoolean(ApplicationProperties.PROPERTY_APP_TRANSACTION_LOG_ENABLED, true);
    }

    @Override
    @NotNull
    public String transactionLogPath() {
        String path = reader.readPropertyAsString(ApplicationProperties.PROPERTY_APP_TRANSACTION_LOG_PATH);
        return path.isBlank() ? "logs/transactions.log" : path;
    }

	@Override
	public boolean includeDatabaseTransactions() {
		return reader.readPropertyAsBoolean(ApplicationProperties.PROPERTY_APP_TRANSACTION_LOG_INCLUDE_DATABASE, true);
	}

	@Override
	public boolean enableCustomPermissions() {
		return reader.readPropertyAsBoolean(ApplicationProperties.PROPERTY_APP_CUSTOM_PERMISSIONS_ENABLED, false);
	}

	@Override
	public boolean enableCustomAppRoles() {
		return reader.readPropertyAsBoolean(ApplicationProperties.PROPERTY_APP_CUSTOM_ROLES_ENABLED, false);
	}

    @Override
    @NotNull
    public ApplicationEnvironment environment() {
        String env = reader.readPropertyAsString(ApplicationProperties.PROPERTY_APP_ENV);
        return env.isEmpty() && env.isBlank() ? ApplicationEnvironment.PROD : ApplicationEnvironment.from(env);
    }
}
