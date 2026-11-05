package com.project.password.manager.configuration.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.util.Logger;

public class PropertiesReader {

    private static final Logger log = Logger.getLogger(PropertiesReader.class);

    public static final String PROPERTY_FILE_PATH = "password-manager.properties";
    private static final Map<String, Object> propertiesMap = new ConcurrentHashMap<>();
    private static volatile boolean initialized;

    private PropertiesReader() {
        log.info("Initializing PropertiesReader");
        initialized = true;
        extractProperties();
    }

    private static class HOLDER {
        private static PropertiesReader instance = new PropertiesReader();
    }

    @NotNull
    public static PropertiesReader getInstance() {
        log.debug("Returning PropertiesReader singleton instance");
        return HOLDER.instance;
    }

    @NotNull
    private File getPropertiesFile() {
        return resolvePropertiesFile();
    }

    @NotNull
    public static File resolvePropertiesFile() {
        File file = new File(Workspace.getInstance().getRoot(), PROPERTY_FILE_PATH);
        log.debug("Resolved properties file path: " + file.getAbsolutePath());
        return file;
    }

    @NotNull
    public static Properties loadProperties() throws IOException {
        Properties properties = new Properties();
        File propertiesFile = resolvePropertiesFile();
        if (!propertiesFile.exists()) {
            return properties;
        }

        try (InputStream propertiesFileStream = new FileInputStream(propertiesFile)) {
            properties.load(propertiesFileStream);
        }
        return properties;
    }

    public static void storeProperties(@NotNull Properties properties, @NotNull String comments) throws IOException {
        File propertiesFile = resolvePropertiesFile();
        try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
            properties.store(fos, comments);
        }
    }

    public static void refreshIfInitialized() {
        if (initialized) {
            getInstance().reload();
        }
    }

    @NotNull
    public String readPropertyAsString(@NotNull String key) {
        Object value = readPropertiesFromPropertiesMap(key);
        log.debug("Reading property [String] key=" + key + " value=" + value);
        return value != null ? value.toString() : "";
    }

    public long readPropertyAsLong(@NotNull String key, long defaultValue) {
        Object value = readPropertiesFromPropertiesMap(key);
        if (value != null) {
            log.debug("Reading property [Long] key=" + key + " value=" + value);
            return Long.parseLong(value.toString());
        }
        log.warn("Property not found for key=" + key + ", using default=" + defaultValue);
        return defaultValue;
    }

    public int readPropertyAsIntger(@NotNull String key, int defaultValue) {
        Object value = readPropertiesFromPropertiesMap(key);
        if (value != null) {
            log.debug("Reading property [Integer] key=" + key + " value=" + value);
            return Integer.parseInt(value.toString());
        }
        log.warn("Property not found for key=" + key + ", using default=" + defaultValue);
        return defaultValue;
    }

    public boolean readPropertyAsBoolean(@NotNull String key, boolean defaultValue) {
        Object value = readPropertiesFromPropertiesMap(key);
        if (value != null) {
            log.debug("Reading property [Boolean] key=" + key + " value=" + value);
            return Boolean.parseBoolean(value.toString());
        }
        log.warn("Property not found for key=" + key + ", using default=" + defaultValue);
        return defaultValue;
    }

    @NotNull
    public List<String> readPropertyAsList(@NotNull String key, @NotNull String delimiter) {
        return readPropertyAsList(key, delimiter, Function.identity());
    }

    @NotNull
    private <T> List<T> readPropertyAsList(@NotNull String key, @NotNull String delimiter, @NotNull Function<String, T> mapper) {
        String value = (String) readPropertiesFromPropertiesMap(key);
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(delimiter)).map(String::trim).filter(v -> !v.isEmpty()).map(mapper)
                .collect(Collectors.toList());
    }

    @NotNull
    public Map<String, String> readPropertyAsMap(@NotNull String key, @NotNull String entryDelimiter, @NotNull String keyValueDelimiter) {

        String value = (String) readPropertiesFromPropertiesMap(key);

        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        return Arrays.stream(value.split(entryDelimiter)).map(String::trim).filter(v -> !v.isEmpty())
                .map(entry -> entry.split(keyValueDelimiter, 2))
                .collect(Collectors.toMap(arr -> arr[0].trim(), arr -> arr.length > 1 ? arr[1].trim() : ""));
    }

    private void extractProperties() {
        log.info("Extracting application properties");
        if (!propertyFileExist()) {
            String msg = "Properties file does not exist in workspace: " + getPropertiesFile().getAbsolutePath();
            log.error(msg);
            throw new RuntimeException(msg);
        }

        try {
            Properties properties = loadProperties();
            properties.entrySet().forEach(entry -> {
                propertiesMap.put(entry.getKey().toString(), entry.getValue());
                log.debug("Loaded property: " + entry.getKey() + "=" + entry.getValue());
            });

            log.info("Successfully loaded " + propertiesMap.size() + " properties");
        } catch (Exception ex) {
            log.error("Error reading the properties file", ex);
            throw new RuntimeException("Error reading the properties file", ex);
        }
    }

    private void reload() {
        propertiesMap.clear();
        extractProperties();
    }

    private boolean propertyFileExist() {
        Workspace workspace = Workspace.getInstance();
        boolean exists = workspace.workspaceExists()
                && workspace.fileExists(new File(workspace.getRoot(), PROPERTY_FILE_PATH));
        log.debug("Properties file existence check -> " + exists);
        return exists;
    }

    @Nullable
    private static Object readPropertiesFromPropertiesMap(@NotNull String key) {
        if (propertiesMap.containsKey(key)) {
            return propertiesMap.get(key);
        }
        return null;
    }
}
