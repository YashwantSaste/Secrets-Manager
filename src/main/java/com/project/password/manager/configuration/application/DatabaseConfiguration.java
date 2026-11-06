package com.project.password.manager.configuration.application;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.configuration.IDatabaseConfiguration;

public class DatabaseConfiguration implements IDatabaseConfiguration {

    @NotNull
    private final PropertiesReader reader;

    public DatabaseConfiguration(@NotNull PropertiesReader reader) {
        this.reader = reader;
    }

    @Override
    public boolean databaseEnabled() {
        return reader.readPropertyAsBoolean(ApplicationProperties.PROPERTY_DATABASE_ENABLED, false);
    }

    @Override
    @NotNull
    public String type() {
        String databaseTypeProperty = reader.readPropertyAsString(ApplicationProperties.PROPERTY_DATABASE_TYPE);
        if (!IDatabaseConfiguration.DATABASE_TYPE_SQL.equalsIgnoreCase(databaseTypeProperty)
                && !IDatabaseConfiguration.DATABASE_TYPE_NO_SQL.equalsIgnoreCase(databaseTypeProperty)) {
            throw new UnsupportedOperationException("Unsupported Database Type");
        }
        return databaseTypeProperty;
    }

    @Override
    @NotNull
    public String username() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_DATABASE_USERNAME);
    }

    @Override
    @NotNull
    public String password() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_DATABASE_PASSWORD);
    }

    @Override
    @NotNull
    public String vendor() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_DATABASE_VENDOR);
    }

    @Override
    public int port() {
        return reader.readPropertyAsIntger(ApplicationProperties.PROPERTY_DATABASE_PORT, 5432);
    }

    @Override
    @NotNull
    public String host() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_DATABASE_HOST);
    }

    @Override
    @NotNull
    public String databaseName() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_DATABASE_NAME);
    }

    @Override
    @NotNull
    public String ddlMode() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_DATABASE_DDL_MODE);
    }

    @Override
    public boolean formatSql() {
        return reader.readPropertyAsBoolean(ApplicationProperties.PROPERTY_DATABASE_SQL_SHOW, false);
    }

    @Override
    public boolean showSql() {
        return reader.readPropertyAsBoolean(ApplicationProperties.PROPERTY_DATABASE_SQL_FORMAT, false);
    }

}
