package com.project.password.manager.database.file.storage;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;
import com.project.password.manager.model.database.file.storage.Token;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class TokenRepository extends FileStorageRepository<Token, String> {

    @Inject
    public TokenRepository(@NotNull IWorkspaceLocator workspaceLocator) {
        super(workspaceLocator);
    }

    /*As we are storing the token inside the user folder in the workspace so the
     * {@code id} here would represent the user id under
     *  which we are storing the token*/
    @Override
    @NotNull
    protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
        return workspaceLocator.getUserWorkspaceLocation();
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
