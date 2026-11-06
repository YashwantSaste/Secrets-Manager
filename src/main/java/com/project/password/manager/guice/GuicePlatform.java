package com.project.password.manager.guice;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.project.password.manager.configuration.IAppConfiguration.ApplicationEnvironment;
import com.project.password.manager.configuration.application.Configuration;

public class GuicePlatform {

    private GuicePlatform() {
    }

    private static final class HOLDER {
	private static final Injector INJECTOR = Guice
		.createInjector(moduleFor(Configuration.getInstance().appConfiguration().environment()));
    }

    @NotNull
    private static GuiceModule moduleFor(@NotNull ApplicationEnvironment environment) {
	return switch (environment) {
	case DEV -> new DevGuiceModule();
	case TEST -> new TestGuiceModule();
	case PROD -> new ProdGuiceModule();
	};
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
