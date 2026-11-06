package com.project.password.manager.database.file.storage;

import java.io.File;

import jakarta.validation.constraints.NotNull;

import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.database.file.storage.UserGroup;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class UserGroupRepository extends FileStorageRepository<UserGroup, String> {

	private static final String USER_GROUP_WORKSPACE_FOLDER = "groups";

	public UserGroupRepository(@NotNull IWorkspaceLocator locator, @NotNull ITransactionLogger transactionLogger) {
		super(locator, transactionLogger);
	}

	@Override
	@NotNull
	protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
		return new File(new File(workspaceLocator.getWorkspaceRoot(), USER_GROUP_WORKSPACE_FOLDER), id);
	}

	@Override
	@NotNull
	protected Class<UserGroup> getEntityClass() {
		return UserGroup.class;
	}

	@Override
	@NotNull
	protected String getEntityFileName() {
		return "group.json";
	}
}
