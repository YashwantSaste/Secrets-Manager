package com.project.password.manager.database.file.storage;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.database.file.storage.User;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class UserRepository extends FileStorageRepository<User, String> {

    @Inject
    public UserRepository(@NotNull IWorkspaceLocator workspaceLocator, @NotNull ITransactionLogger transactionLogger) {
	super(workspaceLocator, transactionLogger);
    }

    @Override
    @NotNull
    protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
	return new File(workspaceLocator.getUserWorkspaceLocation(), id);
    }

    @Override
    @NotNull
    protected Class<User> getEntityClass() {
	return User.class;
    }

    @Override
    @NotNull
    protected String getEntityFileName() {
	return "user.json";
    }

}
