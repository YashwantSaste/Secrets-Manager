package com.project.password.manager.database.file.storage;

import java.io.File;

import jakarta.validation.constraints.NotNull;

import com.google.inject.Inject;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.database.file.storage.Token;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class TokenRepository extends FileStorageRepository<Token, String> {

    @Inject
    public TokenRepository(@NotNull IWorkspaceLocator workspaceLocator, @NotNull ITransactionLogger transactionLogger) {
	super(workspaceLocator, transactionLogger);
    }

    /**
     * Resolves the user's workspace directory where the token is stored. The
     * {@code id} represents the user ID whose token is being stored.
     *
     * @param id the user ID associated with the token
     * @return the user's workspace directory
     */
    @Override
    @NotNull
    protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
	return new File(workspaceLocator.getUserWorkspaceLocation(), id);
    }

    @Override
    @NotNull
    protected Class<Token> getEntityClass() {
	return Token.class;
    }

    @Override
    @NotNull
    protected String getEntityFileName() {
	return "token.json";
    }

}
