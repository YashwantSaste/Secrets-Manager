package com.project.password.manager.database.file.storage;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.inject.Inject;
import com.project.password.manager.model.database.file.storage.Vault;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class VaultRepository extends FileStorageRepository<Vault, String> {

    @Inject
    public VaultRepository(@NotNull IWorkspaceLocator workspaceLocator) {
        super(workspaceLocator);
    }

    @Override
    @NotNull
    protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
        return workspaceLocator.getVaultWorkspaceLocation();
    }

    @Override
    @NotNull
    protected Class<Vault> getEntityClass() {
        return Vault.class;
    }

    @Override
    @Nullable
    public Vault findById(@NotNull String id) {
        File entityDir = resolveEntityDirectoryInFileSystem(id);
        if (!entityDir.exists()) {
            return null;
        }
        File entityFile = new File(entityDir, id + ".json");
        fileManager = new FileManager<>(entityFile, getEntityClass());
        return fileManager.readFromFile();
    }

    @Override
    @NotNull
    protected String getEntityFileName() {
        return "vault.json";
    }

}
