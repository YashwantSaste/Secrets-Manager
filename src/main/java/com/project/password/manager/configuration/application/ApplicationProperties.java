package com.project.password.manager.configuration.application;

import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public class ApplicationProperties {

    /** Application Configurations **/
    public static final String PROPERTY_APP_NAME = "app.name";
    public static final String PROPERTY_APP_VERSION = "app.version";
    public static final String PROPERTY_APP_ENV = "app.env";
    public static final String PROPERTY_APP_CLI_THEME = "app.cli.theme";
    public static final String PROPERTY_APP_CLI_ENABLED = "app.cli.enabled";
    public static final String PROPERTY_APP_CLI_DISPLAY_PROMPT = "app.cli.prompt";

    /** Database Configurations **/
    public static final String PROPERTY_DATABASE_ENABLED = "app.database.enabled";
    public static final String PROPERTY_DATABASE_TYPE = "app.database.type";
    public static final String PROPERTY_DATABASE_USERNAME = "app.database.username";
    public static final String PROPERTY_DATABASE_PASSWORD = "app.database.password";
    public static final String PROPERTY_DATABASE_PORT = "app.database.port";
    public static final String PROPERTY_DATABASE_VENDOR = "app.database.vendor";
    public static final String PROPERTY_DATABASE_NAME = "app.database.name";
    public static final String PROPERTY_DATABASE_HOST = "app.database.host";
    public static final String PROPERTY_DATABASE_DDL_MODE = "app.database.ddl.mode";
    public static final String PROPERTY_DATABASE_SQL_SHOW = "app.database.sql.show";
    public static final String PROPERTY_DATABASE_SQL_FORMAT = "app.database.sql.format";
    /** Argon2 Configurations **/
    public static final String PROPERTY_ARGON2_MEMORY_SIZE = "app.crypto.argon2.memory.kb";
    public static final String PROPERTY_ARGON2_ITERATIONS = "app.crypto.argon2.iterations";
    public static final String PROPERTY_ARGON2_PARALLELISM = "app.crypto.argon2.parallelism";
    public static final String PROPERTY_ARGON2_SALT_LENGTH = "app.crypto.argon2.salt.length";
    public static final String PROPERTY_ARGON2_HASH_LENGTH = "app.crypto.argon2.hash.length";
    /** JWT Configurations **/
    public static final String PROPERTY_JWT_ALGORITHM = "app.jwt.algorithm";
    public static final String PROPERTY_JWT_ISSUER = "app.jwt.issuer";
    public static final String PROPERTY_JWT_REFRESH_EXPIRATION = "app.jwt.refresh.expiration.ms";
    public static final String PROPERTY_JWT_ACCESS_EXPIRATION = "app.jwt.access.expiration.ms";
    public static final String PROPERTY_JWT_SECRET = "app.jwt.secret";
    public static final String PROPERTY_JWT_PRIVATE_KEY_PATH = "app.jwt.private.key.path";
    public static final String PROPERTY_JWT_PUBLIC_KEY_PATH = "app.jwt.public.key.path";
    /** AES Configurations **/
    public static final String PROPERTY_AES_TRANSFORMATION = "app.aes.transformation";
    public static final String PROPERTY_AES_KEY_SIZE = "app.aes.key.size";
    public static final String PROPERTY_AES_TAG_LENGTH = "app.aes.tag.length";
    public static final String PROPERTY_AES_IV_LENGTH = "app.aes.iv.length";
    /** Salt key generation Configurations */
    public static final String PROPERTY_SALT_KEY_ITERATIONS = "app.salt.iterations";
    public static final String PROPERTY_SALT_KEY_LENGTH = "app.salt.key.size";
    /** OAuth2 Configurations */
    public static final String PROPERTY_OAUTH2_TOKEN_URL = "app.oauth2.token.url";
    public static final String PROPERTY_OAUTH2_CLIENT_ID = "app.oauth2.client.id";
    public static final String PROPERTY_OAUTH2_CLIENT_SECRET = "app.oauth2.client.secret";
    public static final String PROPERTY_OAUTH2_AUTHORIZE_URL = "app.oauth2.authorize.url";
    public static final String PROPERTY_OAUTH2_USER_URL = "app.oauth2.user.url";
    public static final String PROPERTY_OAUTH2_DEVICE_CODE_URL = "app.oauth2.device.code.url";
    public static final String PROPERTY_OAUTH2_SCOPES = "app.oauth2.scopes";
    public static final String PROPERTY_OAUTH2_TOKEN_PARAMTERS = "app.oauth2.token.parameters";

    private static final List<String> SUPPORTED_KEYS = List.of(
            PROPERTY_APP_NAME,
            PROPERTY_APP_VERSION,
            PROPERTY_APP_CLI_THEME,
            PROPERTY_APP_CLI_ENABLED,
            PROPERTY_APP_CLI_DISPLAY_PROMPT,
            PROPERTY_DATABASE_ENABLED,
            PROPERTY_DATABASE_TYPE,
            PROPERTY_DATABASE_USERNAME,
            PROPERTY_DATABASE_PASSWORD,
            PROPERTY_DATABASE_PORT,
            PROPERTY_DATABASE_VENDOR,
            PROPERTY_DATABASE_NAME,
            PROPERTY_DATABASE_HOST,
            PROPERTY_DATABASE_DDL_MODE,
            PROPERTY_ARGON2_MEMORY_SIZE,
            PROPERTY_ARGON2_ITERATIONS,
            PROPERTY_ARGON2_PARALLELISM,
            PROPERTY_ARGON2_SALT_LENGTH,
            PROPERTY_ARGON2_HASH_LENGTH,
            PROPERTY_JWT_ALGORITHM,
            PROPERTY_JWT_ISSUER,
            PROPERTY_JWT_REFRESH_EXPIRATION,
            PROPERTY_JWT_ACCESS_EXPIRATION,
            PROPERTY_JWT_SECRET,
            PROPERTY_JWT_PRIVATE_KEY_PATH,
            PROPERTY_JWT_PUBLIC_KEY_PATH,
            PROPERTY_AES_TRANSFORMATION,
            PROPERTY_AES_KEY_SIZE,
            PROPERTY_AES_TAG_LENGTH,
            PROPERTY_AES_IV_LENGTH,
            PROPERTY_SALT_KEY_ITERATIONS,
            PROPERTY_SALT_KEY_LENGTH,
            PROPERTY_OAUTH2_TOKEN_URL,
            PROPERTY_OAUTH2_CLIENT_SECRET,
            PROPERTY_OAUTH2_AUTHORIZE_URL,
            PROPERTY_OAUTH2_USER_URL,
            PROPERTY_OAUTH2_DEVICE_CODE_URL,
            PROPERTY_OAUTH2_SCOPES,
            PROPERTY_OAUTH2_TOKEN_PARAMTERS, PROPERTY_DATABASE_SQL_SHOW, PROPERTY_DATABASE_SQL_FORMAT);

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            PROPERTY_DATABASE_PASSWORD, PROPERTY_OAUTH2_CLIENT_ID,
            PROPERTY_JWT_SECRET, PROPERTY_OAUTH2_CLIENT_SECRET);

    private ApplicationProperties() {
    }

    public static List<String> supportedKeys() {
        return SUPPORTED_KEYS;
    }

    public static boolean isSupportedKey(@NotNull String key) {
        return SUPPORTED_KEYS.contains(key);
    }

    public static boolean isSensitiveKey(@NotNull String key) {
        return SENSITIVE_KEYS.contains(key);
    }
}
