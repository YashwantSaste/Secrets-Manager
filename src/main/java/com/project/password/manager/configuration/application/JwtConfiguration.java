package com.project.password.manager.configuration.application;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.auth.jwt.JwtAlgorithm;
import com.project.password.manager.configuration.IJwtConfiguration;

public class JwtConfiguration implements IJwtConfiguration {

    private final PropertiesReader reader;

    public JwtConfiguration(@NotNull PropertiesReader reader) {
        this.reader = reader;
    }

    @Override
    @NotNull
    public JwtAlgorithm algorithm() {
        String value = reader.readPropertyAsString(ApplicationProperties.PROPERTY_JWT_ALGORITHM);
        return JwtAlgorithm.from(value);
    }

    @Override
    @NotNull
    public String issuer() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_JWT_ISSUER);
    }

    @Override
    public long accessExpirationMs() {
        return reader.readPropertyAsLong(ApplicationProperties.PROPERTY_JWT_ACCESS_EXPIRATION, 900000);
    }

    @Override
    public long refreshExpirationMs() {
        return reader.readPropertyAsLong(ApplicationProperties.PROPERTY_JWT_REFRESH_EXPIRATION, 604800000);
    }

    @Override
    @NotNull
    public String secret() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_JWT_SECRET);
    }

    @Override
    @NotNull
    public String privateKeyPath() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_JWT_PRIVATE_KEY_PATH);
    }

    @Override
    @NotNull
    public String publicKeyPath() {
        return reader.readPropertyAsString(ApplicationProperties.PROPERTY_JWT_PUBLIC_KEY_PATH);
    }
}
