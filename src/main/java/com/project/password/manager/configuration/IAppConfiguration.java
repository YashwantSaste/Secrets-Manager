package com.project.password.manager.configuration;

import org.jetbrains.annotations.NotNull;

public interface IAppConfiguration {

    @NotNull
    String name();

    @NotNull
    String version();

    @NotNull
    ApplicationEnvironment environment();

    enum ApplicationEnvironment {

        DEV("dev"), TEST("test"), PROD("prod");

        @NotNull
        private final String value;

        ApplicationEnvironment(@NotNull String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @NotNull
        public static ApplicationEnvironment from(@NotNull String appEnvironment) {
            for (ApplicationEnvironment env : values()) {
                if (env.value.equalsIgnoreCase(appEnvironment.trim())) {
                    return env;
                }
            }
            throw new IllegalArgumentException("Unsupported application environment: '" + appEnvironment + "'. Expected one of: dev, test, prod");
        }
    }

}
