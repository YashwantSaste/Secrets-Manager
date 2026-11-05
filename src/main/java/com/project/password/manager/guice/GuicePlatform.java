package com.project.password.manager.guice;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.project.password.manager.configuration.IAppConfiguration.ApplicationEnvironment;
import com.project.password.manager.configuration.IConfiguration;
import com.project.password.manager.configuration.application.Configuration;

public class GuicePlatform {

    private GuicePlatform() {
    }

    private static final class HOLDER {
        static IConfiguration configuration = Configuration.getInstance();
        static ApplicationEnvironment environment = configuration.appConfiguration().environment();
        static Module module = switch (environment) {
            case PROD -> new GuiceModule();
            default -> new GuiceModule();
        };
        private static final Injector INJECTOR = Guice.createInjector(module);
    }

    @NotNull
    private static Injector injector() {
        return HOLDER.INJECTOR;
    }

    @NotNull
    public static Injector getInjector() {
        return injector();
    }

    @NotNull
    public static <T> T getInstance(@NotNull Class<T> type) {
        return Objects.requireNonNull(injector().getInstance(type));
    }

    public static void injectMembers(@NotNull Object object) {
        injector().injectMembers(object);
    }

}
