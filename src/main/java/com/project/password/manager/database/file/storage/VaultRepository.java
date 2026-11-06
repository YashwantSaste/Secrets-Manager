package com.project.password.manager.database.file.storage;

import java.util.ArrayList;
import java.io.File;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.inject.Inject;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.database.file.storage.Vault;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class VaultRepository extends FileStorageRepository<Vault, String> {

    @Inject
    public VaultRepository(@NotNull IWorkspaceLocator workspaceLocator, @NotNull ITransactionLogger transactionLogger) {
	super(workspaceLocator, transactionLogger);
    }

    @Override
    @NotNull
    protected File resolveEntityDirectoryInFileSystem(@NotNull String id) {
	return new File(workspaceLocator.getVaultWorkspaceLocation(), id);
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
	    logRepositoryOperation("findById", getEntityClass().getSimpleName(), id, "MISS", null);
	    return null;
	}
	File entityFile = new File(entityDir, id + ".json");
	fileManager = new FileManager<>(entityFile, getEntityClass());
	Vault vault = fileManager.readFromFile();
	logRepositoryOperation("findById", getEntityClass().getSimpleName(), id, vault == null ? "MISS" : "SUCCESS",
		entityFile.getAbsolutePath());
	return vault;
    }

    @Override
    @NotNull
    public List<Vault> findAll() {
        List<Vault> vaults = new ArrayList<>();
        File[] vaultDirectories = workspaceLocator.getVaultWorkspaceLocation().listFiles(File::isDirectory);
        if (vaultDirectories == null) {
            return vaults;
        }
        for (File vaultDirectory : vaultDirectories) {
            File entityFile = new File(vaultDirectory, vaultDirectory.getName() + ".json");
            if (!entityFile.isFile()) {
                continue;
            }
            Vault vault = new FileManager<>(entityFile, getEntityClass()).readFromFile();
            if (vault != null) {
                vaults.add(vault);
            }
        }
        return vaults;
    }

    @Override
    @NotNull
    protected String getEntityFileName() {
        return "vault.json";
    }

}
