package com.project.password.manager.guice;

import com.project.password.manager.configuration.IAppConfiguration.ApplicationEnvironment;

/** Dependency bindings for tests; extend this module to replace production dependencies. */
public class TestGuiceModule extends GuiceModule {
    @Override
    protected void configureEnvironment(ApplicationEnvironment environment) {
        // Add test-only bindings here, or subclass this module in test sources.
    }
}
