package com.project.password.manager.guice;

import com.project.password.manager.configuration.IAppConfiguration.ApplicationEnvironment;

/** Dependency bindings for local development. */
public class DevGuiceModule extends GuiceModule {
    @Override
    protected void configureEnvironment(ApplicationEnvironment environment) {
        // Add development-only bindings here.
    }
}
