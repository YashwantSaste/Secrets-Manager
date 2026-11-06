package com.project.password.manager.configuration.application;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.configuration.IArgon2Configuration;

public class Argon2Configuration implements IArgon2Configuration {

    @NotNull
    private final PropertiesReader propertiesReader;

    public Argon2Configuration(@NotNull PropertiesReader propertiesReader) {
        this.propertiesReader = propertiesReader;
    }

    @Override
    public int memoryKb() {
        return readProperty(ApplicationProperties.PROPERTY_ARGON2_MEMORY_SIZE, 60 * 1024);
    }

    @Override
    public int iterations() {
        return readProperty(ApplicationProperties.PROPERTY_ARGON2_ITERATIONS, 3);
    }

    @Override
    public int parallelism() {
        return readProperty(ApplicationProperties.PROPERTY_ARGON2_PARALLELISM, 1);
    }

    @Override
    public int saltLengthBytes() {
        return readProperty(ApplicationProperties.PROPERTY_ARGON2_SALT_LENGTH, 16);

    }

    @Override
    public int hashLengthBytes() {
        return readProperty(ApplicationProperties.PROPERTY_ARGON2_HASH_LENGTH, 32);
    }

    private int readProperty(@NotNull String propertyName, int defaultValue) {
        return propertiesReader.readPropertyAsIntger(propertyName, defaultValue);
    }

}
