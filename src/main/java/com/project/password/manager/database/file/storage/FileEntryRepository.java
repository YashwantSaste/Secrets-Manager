package com.project.password.manager.database.file.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.inject.Inject;
import com.project.password.manager.database.EntryDataRepository;
import com.project.password.manager.database.EntryStorageKey;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.model.entry.EncryptedEntryRecord;
import com.project.password.manager.workspace.IWorkspaceLocator;

public class FileEntryRepository implements EntryDataRepository {

    @NotNull
    private final File workspace;
    @NotNull
    private final ITransactionLogger logger;

    @Inject
    public FileEntryRepository(@NotNull IWorkspaceLocator locator, @NotNull ITransactionLogger logger) {
        this.logger = logger;
        workspace = locator.getEntriesWorkspaceLocation();
    }

    @Override
    public void save(@NotNull EncryptedEntryRecord record) {
        File entryFile = resolveEntryFile(record.getVaultId(), record.getId());
        entryFile.getParentFile().mkdirs();
        new FileManager<>(entryFile, EncryptedEntryRecord.class).writeToFile(record);
    }

    @Override
    @Nullable
    public EncryptedEntryRecord findById(@NotNull EntryStorageKey key) {
        File entryFile = resolveEntryFile(key.vaultId(), key.entryId());
        if (!entryFile.exists()) {
            return null;
        }
        return new FileManager<>(entryFile, EncryptedEntryRecord.class).readFromFile();
    }

    @Override
    @NotNull
    public List<EncryptedEntryRecord> findAll() {
        List<EncryptedEntryRecord> entries = new ArrayList<>();
        File[] vaultDirectories = workspace.listFiles(File::isDirectory);
        if (vaultDirectories == null) {
            return entries;
        }
        for (File vaultDirectory : vaultDirectories) {
            File[] entryFiles = vaultDirectory.listFiles((dir, name) -> name.endsWith(".json"));
            if (entryFiles == null) {
                continue;
            }
            for (File entryFile : entryFiles) {
                EncryptedEntryRecord entry = new FileManager<>(entryFile, EncryptedEntryRecord.class).readFromFile();
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    @Override
    @NotNull
    public List<EncryptedEntryRecord> findByVaultId(@NotNull String vaultId) {
        List<EncryptedEntryRecord> entries = new ArrayList<>();
        File vaultDirectory = new File(workspace, vaultId);
        File[] entryFiles = vaultDirectory.listFiles((dir, name) -> name.endsWith(".json"));
        if (entryFiles == null) {
            return entries;
        }
        for (File entryFile : entryFiles) {
            EncryptedEntryRecord entry = new FileManager<>(entryFile, EncryptedEntryRecord.class).readFromFile();
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    @Override
    public void delete(@NotNull EntryStorageKey key) {
        deleteByKey(key);
    }

    @Override
    public void update(@NotNull EntryStorageKey key, @NotNull EncryptedEntryRecord entity) {
        save(entity);
    }

    public void deleteByKey(@NotNull EntryStorageKey key) {
        File entryFile = resolveEntryFile(key.vaultId(), key.entryId());
        new FileManager<>(entryFile, EncryptedEntryRecord.class).delete();
    }

    @NotNull
    private File resolveEntryFile(@NotNull String vaultId, @NotNull String entryId) {
        return new File(new File(workspace, vaultId), entryId + ".json");
    }

}
