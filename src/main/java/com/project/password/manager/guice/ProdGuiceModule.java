package com.project.password.manager.guice;

import com.project.password.manager.configuration.IAppConfiguration.ApplicationEnvironment;

/** Dependency bindings for production. */
public class ProdGuiceModule extends GuiceModule {
    @Override
    protected void configureEnvironment(ApplicationEnvironment environment) {
        // Add production-only bindings here.
    }
}
