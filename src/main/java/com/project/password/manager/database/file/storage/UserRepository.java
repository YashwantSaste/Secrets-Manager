package com.project.password.manager.database.file.storage;

import java.io.File;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.model.database.file.storage.User;

public class UserRepository extends FileStorageRepository<User, String> {

    private static final String USERS_WORKSPACE_FOLDER = "users";

    public UserRepository(@NotNull File workspace) {
        super(new File(workspace, USERS_WORKSPACE_FOLDER));
    }

    @Override
    @NotNull
    protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
        return new File(workspace, id);
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
