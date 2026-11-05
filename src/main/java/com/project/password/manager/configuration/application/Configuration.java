package com.project.password.manager.configuration.application;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.configuration.IAESConfiguration;
import com.project.password.manager.configuration.IAppConfiguration;
import com.project.password.manager.configuration.IArgon2Configuration;
import com.project.password.manager.configuration.ICLIConfiguration;
import com.project.password.manager.configuration.IConfiguration;
import com.project.password.manager.configuration.IDatabaseConfiguration;
import com.project.password.manager.configuration.IJwtConfiguration;
import com.project.password.manager.configuration.IOAuth2Configuration;
import com.project.password.manager.configuration.ISaltKeyConfiguration;

public class Configuration implements IConfiguration {

    @NotNull
    private final PropertiesReader propertiesReader = PropertiesReader.getInstance();

    private Configuration() {
    }

    private static class Holder {
        private static final Configuration INSTANCE = new Configuration();
    }

    @NotNull
    public static Configuration getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    @NotNull
    public IAppConfiguration appConfiguration() {
        return new ApplicationConfiguration(propertiesReader);
    }

    @Override
    @NotNull
    public ICLIConfiguration cliConfiguration() {
        return new CLIConfiguration(propertiesReader);
    }

    @Override
    @NotNull
    public IDatabaseConfiguration databaseConfiguration() {
        return new DatabaseConfiguration(propertiesReader);
    }

    @Override
    @NotNull
    public IArgon2Configuration argon2Configuration() {
        return new Argon2Configuration(propertiesReader);
    }

    @Override
    @NotNull
    public IJwtConfiguration jwtConfiguration() {
        return new JwtConfiguration(propertiesReader);
    }

    @Override
    @NotNull
    public IAESConfiguration aesConfiguration() {
        return new AESConfiguration(propertiesReader);
    }

    @Override
    @NotNull
    public ISaltKeyConfiguration saltKeyConfiguration() {
        return new SaltKeyConfigurations(propertiesReader);
    }

    @Override
    @NotNull
    public IOAuth2Configuration oauth2Configuration() {
        return new OAuth2Configuration(propertiesReader);
    }
}
