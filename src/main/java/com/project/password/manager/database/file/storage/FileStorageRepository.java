package com.project.password.manager.database.file.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.database.DataRepository;
import com.project.password.manager.model.database.file.storage.IFileStorableEntity;
import com.project.password.manager.util.Logger;
import com.project.password.manager.util.MetadataListener;

public abstract class FileStorageRepository<T extends IFileStorableEntity, Id> implements DataRepository<T, Id> {

    private static final Logger log = Logger.getLogger("FileRepository class");
    @NotNull
    protected final File workspace;
    @Nullable
    protected FileManager<T> fileManager;

    protected FileStorageRepository(@NotNull File workspace) {
        this.workspace = workspace;
    }

    @Override
    public void save(@NotNull T entity) {
        isValidId(entity);
        MetadataListener.beforeCreate(entity);
        File entityDirectory = resolveEntityDirectoryInFileSystem(entity.getId());
        entityDirectory.mkdirs();
        File entityFile = new File(entityDirectory, entity.getFileName());
        fileManager = new FileManager<>(entityFile, getEntityClass());
        fileManager.writeToFileIfExist(entityFile);
    }

    @Override
    @Nullable
    public T findById(@NotNull Id id) {
        File entityDir = resolveEntityDirectoryInFileSystem(id.toString());
        if (!entityDir.exists()) {
            log.debug("File related to related ID does not exist in the workspace");
            return null;
        }
        File entityFile = new File(entityDir, getEntityFileName());
        fileManager = new FileManager<T>(entityFile, getEntityClass());
        return fileManager.readFromFile();
    }

    @Override
    @NotNull
    public List<T> findAll() {
        List<T> entities = new ArrayList<>();
        if (!workspace.exists()) {
            return entities;
        }
        File[] entityDirectories = workspace.listFiles(File::isDirectory);
        if (entityDirectories == null) {
            return entities;
        }
        for (File entityDirectory : entityDirectories) {
            File entityFile = new File(entityDirectory, getEntityFileName());
            if (!entityFile.exists()) {
                continue;
            }
            FileManager<T> currentFileManager = new FileManager<>(entityFile, getEntityClass());
            @Nullable
            T entity = currentFileManager.readFromFile();
            if (entity != null) {
                entities.add(entity);
            }
        }
        return entities;
    }

    @Override
    public void delete(@NotNull Id id) {
        File entityDirectory = resolveEntityDirectoryInFileSystem(id.toString());
        File entityFile = new File(entityDirectory, getEntityFileName());
        fileManager = new FileManager<>(entityFile, getEntityClass());
        fileManager.delete();
    }

    @Override
    public void update(@NotNull Id id, @NotNull T entity) {
        isValidId(entity);
        MetadataListener.beforeUpdate(entity);
        File entityDirectory = resolveEntityDirectoryInFileSystem(id.toString());
        entityDirectory.mkdirs();
        File entityFile = new File(entityDirectory, entity.getFileName());
        fileManager = new FileManager<>(entityFile, getEntityClass());
        fileManager.writeToFile(entity);
    }

    protected void isValidId(@NotNull T entity) {
        if (entity.getId().isEmpty() && entity.getFileName().isEmpty()) {
            throw new IllegalStateException("Invalid entity format: Both ID and file name are missing.");
        }
    }

    @NotNull
    protected abstract File resolveEntityDirectoryInFileSystem(@NotNull String id);

    @NotNull
    protected abstract Class<T> getEntityClass();

    @NotNull
    protected abstract String getEntityFileName();
}
