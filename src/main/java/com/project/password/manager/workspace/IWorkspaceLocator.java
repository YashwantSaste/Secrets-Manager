package com.project.password.manager.workspace;

import java.io.File;

import org.jetbrains.annotations.NotNull;

public interface IWorkspaceLocator {

    @NotNull
    String USERS_WORKSPACE_FOLDER = "users";

    @NotNull
    String VAULTS_WORKSPACE_FOLDER = "vaults";

    @NotNull
    String ENTRIES_WORKSPACE_FOLDER = "entries";

    @NotNull
    String LOGS_WORKSPACE_FOLDER = "logs";

    @NotNull
    IWorkspace getWorkspace();

    @NotNull
    File getWorkspaceRoot();

    @NotNull
    File getUserWorkspaceLocation();

    @NotNull
    File getVaultWorkspaceLocation();

    @NotNull
    File getEntriesWorkspaceLocation();

    @NotNull
    File getLogsWorkspaceLocation();
}
