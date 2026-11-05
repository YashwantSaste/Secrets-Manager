/*
 * Copyright 2026 Polarion AG
 */
package com.project.password.manager.workspace;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;

public interface IWorkspaceLocator {

    @NotNull
    String USERS_WORKSPACE_FOLDER = "users";

    @NotNull
    String VAULTS_WORKSPACE_FOLDER = "vaults";

    @NotNull
    String ENTRIES_WORKSPACE_FOLDER = "entries";

    @NotNull
    @Inject
    IWorkspace getWorkspace();

    @NotNull
    File getWorkspaceRoot();

    @NotNull
    File getUserWorkspaceLocation();

    @NotNull
    File getVaultWorkspaceLocation();

    @NotNull
    File getEntriesWorkspaceLocation();
}
