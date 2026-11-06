package com.project.password.manager.workspace;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.exceptions.ApplicationErrorException;
import com.project.password.manager.guice.GuicePlatform;

public class WorkspaceLocator implements IWorkspaceLocator {

    public WorkspaceLocator() {
        if (!getWorkspace().workspaceExists()) {
            throw new ApplicationErrorException("Workspace does not exist");
        }
    }

    @Override
    @NotNull
    public IWorkspace getWorkspace() {
        return GuicePlatform.getInstance(IWorkspace.class);
    }

    @Override
    @NotNull
    public File getWorkspaceRoot() {
        return getWorkspace().getRoot();
    }

    @Override
    @NotNull
    public File getUserWorkspaceLocation() {
        return new File(getWorkspaceRoot(), USERS_WORKSPACE_FOLDER);
    }

    @Override
    @NotNull
    public File getVaultWorkspaceLocation() {
        return new File(getWorkspaceRoot(), VAULTS_WORKSPACE_FOLDER);
    }

    @Override
    @NotNull
    public File getEntriesWorkspaceLocation() {
        return new File(getWorkspaceRoot(), ENTRIES_WORKSPACE_FOLDER);
    }

    @Override
    @NotNull
    public File getLogsWorkspaceLocation() {
        return new File(getWorkspaceRoot(), LOGS_WORKSPACE_FOLDER);
    }

}
