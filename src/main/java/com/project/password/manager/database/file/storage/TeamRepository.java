package com.project.password.manager.database.file.storage;

import java.io.File;

import jakarta.validation.constraints.NotNull;

import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.database.file.storage.Team;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class TeamRepository extends FileStorageRepository<Team, String> {

	private static final String TEAM_WORKSPACE_FOLDER = "teams";

	public TeamRepository(@NotNull IWorkspaceLocator locator, @NotNull ITransactionLogger transactionLogger) {
		super(locator, transactionLogger);
	}

	@Override
	@NotNull
	protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
		return new File(new File(workspaceLocator.getWorkspaceRoot(), TEAM_WORKSPACE_FOLDER), id);
	}

	@Override
	@NotNull
	protected Class<Team> getEntityClass() {
		return Team.class;
	}

	@Override
	@NotNull
	protected String getEntityFileName() {
		return "team.json";
	}

}
