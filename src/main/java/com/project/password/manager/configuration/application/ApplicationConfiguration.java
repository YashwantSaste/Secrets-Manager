package com.project.password.manager.configuration.application;

import org.jetbrains.annotations.NotNull;

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
    @NotNull
    public ApplicationEnvironment environment() {
        String env = reader.readPropertyAsString(ApplicationProperties.PROPERTY_APP_ENV);
        return env.isEmpty() && env.isBlank() ? ApplicationEnvironment.PROD : ApplicationEnvironment.from(env);
    }
}
