package com.project.password.manager.configuration.application;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.util.Logger;

public class Workspace {

	private static final Logger log = Logger.getLogger(Workspace.class);

	private static final String USER_DIR_PATH = System.getProperty("user.home");
	private static final String WORKSPACE_DIR = "password-manager";
	private static final File workspace = new File(USER_DIR_PATH, WORKSPACE_DIR);

	private static class HOLDER {
		private static final Workspace instance = new Workspace();
	}

	private Workspace() {
		log.debug("Workspace instance initialized");
	}

	@NotNull
	public static Workspace getInstance() {
		return HOLDER.instance;
	}

	public boolean workspaceExists() {
		boolean exists = workspace.exists();
		log.debug("Workspace exists check at: " + workspace.getAbsolutePath() + " -> " + exists);
		return exists;
	}

	public boolean fileExists(@NotNull File directory) {
		boolean exists = directory.exists();
		log.debug("File exists check at: " + directory.getAbsolutePath() + " -> " + exists);
		return exists;
	}

	public static void configureWorkSpace() {
		log.info("Checking workspace configuration");

		if (!workspace.exists()) {
			log.info("Workspace not found. Creating workspace at: " + workspace.getAbsolutePath());
			if (workspace.mkdir()) {
				log.info("Workspace successfully created");
				try {
					createPropertiesFile();
				} catch (IOException e) {
					log.error("Failed to create application properties file", e);
					throw new RuntimeException("Error while creating application properties file", e);
				}
			} else {
				String errorMsg = "Failed to create workspace directory\n" + "Path       : "
						+ workspace.getAbsolutePath() + "\n" + "OS User    : " + System.getProperty("user.name") + "\n"
						+ "Resolution : Ensure write permissions for user home directory";
				log.error(errorMsg);
				throw new IllegalStateException(errorMsg);
			}
		} else {
			log.info("Workspace already exists at: " + workspace.getAbsolutePath());
			try {
				createPropertiesFile();
			} catch (IOException e) {
				log.error("Failed to validate application properties file", e);
				throw new RuntimeException("Error while reconciling application properties file", e);
			}
		}
	}

	@NotNull
	public File getRoot() {
		log.debug("Returning workspace root: " + workspace.getAbsolutePath());
		return workspace;
	}

	public static void createDirectories(@NotNull String... folders) {
		for (String name : folders) {
			File dir = new File(workspace, name);
			if (!dir.exists()) {
				log.info("Creating directory: " + dir.getAbsolutePath());
				if (!dir.mkdirs()) {
					log.error("Failed to create directory: " + dir.getAbsolutePath());
					throw new IllegalStateException("Failed to create directory: " + dir.getAbsolutePath());
				}
			} else {
				log.debug("Directory already exists: " + dir.getAbsolutePath());
			}
		}
	}

	private static void createPropertiesFile() throws IOException {
		File propertiesFile = PropertiesReader.resolvePropertiesFile();
		Properties properties = PropertiesReader.loadProperties();
		if (propertiesFile.exists()) {
			log.info("Ensuring application properties file is initialized at: " + propertiesFile.getAbsolutePath());
		} else {
			log.info("Creating application properties file at: " + propertiesFile.getAbsolutePath());
		}

		boolean changed = applyDefaultProperties(properties);
		if (!propertiesFile.exists() || changed) {
			PropertiesReader.storeProperties(properties, "Application properties created during workspace initialization");
			PropertiesReader.refreshIfInitialized();
			log.info("Application properties file created successfully");
		} else {
			log.info("Application properties file already contains all required defaults");
		}
	}

	private static boolean applyDefaultProperties(@NotNull Properties properties) {
		boolean changed = false;
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_VERSION, "1.0.0");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_NAME, "password-manager-cli");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_CLI_THEME, "warm-retro");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_CLI_ENABLED, String.valueOf(true));
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_CLI_DISPLAY_PROMPT, "password-manager-> ");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_TRANSACTION_LOG_ENABLED,
				String.valueOf(true));
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_TRANSACTION_LOG_PATH,
				"logs/transactions.log");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_APP_TRANSACTION_LOG_INCLUDE_DATABASE,
				String.valueOf(true));

		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_ENABLED, "false");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_TYPE, "sql");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_USERNAME, "username");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_PASSWORD, "password");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_PORT, "5432");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_VENDOR, "postgres");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_NAME, "password_manager");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_HOST, "localhost");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_DATABASE_DDL_MODE, "update");

		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_ARGON2_MEMORY_SIZE, "65536");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_ARGON2_ITERATIONS, "3");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_ARGON2_PARALLELISM, "1");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_ARGON2_SALT_LENGTH, "16");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_ARGON2_HASH_LENGTH, "32");

		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_JWT_ALGORITHM, "HS256");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_JWT_ISSUER, "password-manager-cli");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_JWT_REFRESH_EXPIRATION, "604800000");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_JWT_ACCESS_EXPIRATION, "900000");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_JWT_SECRET,
				"change-this-demo-secret-before-sharing-builds");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_JWT_PRIVATE_KEY_PATH, "private.key");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_JWT_PUBLIC_KEY_PATH, "public.key");

		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_AES_TRANSFORMATION, "AES/GCM/NoPadding");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_AES_KEY_SIZE, "128");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_AES_TAG_LENGTH, "128");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_AES_IV_LENGTH, "12");

		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_SALT_KEY_ITERATIONS, "65536");
		changed |= putIfAbsent(properties, ApplicationProperties.PROPERTY_SALT_KEY_LENGTH, "256");
		return changed;
	}

	private static boolean putIfAbsent(@NotNull Properties properties, @NotNull String key, @NotNull String value) {
		if (properties.getProperty(key) != null) {
			return false;
		}
		properties.setProperty(key, value);
		return true;
	}
}
