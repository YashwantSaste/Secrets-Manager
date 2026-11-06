package com.project.password.manager.database.file.storage;

import java.io.File;

import jakarta.validation.constraints.NotNull;

import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.CustomRole;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class CustomRoleRepository extends FileStorageRepository<CustomRole, String> {

	private static final String TEAM_WORKSPACE_FOLDER = "roles";

	public CustomRoleRepository(@NotNull IWorkspaceLocator locator, @NotNull ITransactionLogger transactionLogger) {
		super(locator, transactionLogger);
	}

	@Override
	@NotNull
	protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
		return new File(new File(workspaceLocator.getWorkspaceRoot(), TEAM_WORKSPACE_FOLDER), id);
	}

	@Override
	@NotNull
	protected Class<CustomRole> getEntityClass() {
		return CustomRole.class;
	}

	@Override
	@NotNull
	protected String getEntityFileName() {
		return "role.json";
	}

}
