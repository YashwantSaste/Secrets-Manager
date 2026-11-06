package com.project.password.manager.workspace;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;
import com.project.password.manager.exceptions.ApplicationErrorException;

public class WorkspaceLocator implements IWorkspaceLocator {

    private final IWorkspace workspace;

    @Inject
    public WorkspaceLocator(@NotNull IWorkspace workspace) {
	this.workspace = workspace;
	if (!workspace.workspaceExists()) {
	    throw new ApplicationErrorException("Workspace does not exist");
	}
    }

    @Override
    @NotNull
    public IWorkspace getWorkspace() {
	return workspace;
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

    @Override
    @NotNull
    public File getTeamsWorkspaceLocation() {
	return new File(getWorkspaceRoot(), TEAMS_WORKSPACE_FOLDER);
    }

    @Override
    @NotNull
    public File getUserGroupsWorkspaceLocation() {
	return new File(getWorkspaceRoot(), USER_GROUPS_WORKSPACE_FOLDER);
    }

}
