package com.project.password.manager.database.file.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.util.Logger;
import com.project.password.manager.util.ModelObjectMapperFactory;

public class FileManager<T extends IEntity> {

    private static final Logger log = Logger.getLogger(FileManager.class);
    @NotNull
    private final File file;
    @NotNull
    private final Class<T> clazz;
    @NotNull
    private ObjectMapper mapper;

    public FileManager(@NotNull File file, @NotNull Class<T> clazz) {
        this.file = file;
        this.clazz = clazz;
        mapper = ModelObjectMapperFactory.create();
    }

    public boolean doFileExist() {
        return file.exists();
    }

    /* Returns the object model stored in the file system */
    @Nullable
    public T readFromFile() {
        return readFromInputStreamSafely();
    }

    public void writeToFile(@NotNull Object objectToWrite) {
        createOutputStreamSafely(objectToWrite);
    }

    public void writeToFileIfExist(@NotNull Object objectToWrite) {
        if (doFileExist()) {
            createOutputStreamSafely(objectToWrite);
        }
        log.warn("Given file already exists in the workspace: " + file.getAbsolutePath());
    }

    public void delete() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Nullable
    private T readFromInputStreamSafely() {
        try {
            T retrivedEntity = mapper.readValue(file, clazz);
            return retrivedEntity;
        } catch (JsonParseException | JsonMappingException ex) {
            throw new RuntimeException("Failed to deserialise JSON into object entity: " + ex);
        } catch (FileNotFoundException ex) {
            log.error("The given file does not exists in the workspace: " + file);
            return null;
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error during retriving data to the file system " + ex);
        }
    }

    private void createOutputStreamSafely(@NotNull Object data) {
        try {
            String json = mapper.writeValueAsString(data);
            log.debug("Writing the data to the file: " + file.getAbsoluteFile());
            OutputStream outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (IOException ex) {
            throw new RuntimeException("Error writing the file to the database: " + ex);
        }
    }
}
