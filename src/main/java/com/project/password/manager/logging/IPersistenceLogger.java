package com.project.password.manager.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.event.EventType;

public interface IPersistenceLogger extends ITransactionLogger {

    @Override
    void logRepositoryOperation(boolean databaseOperation, @NotNull String repositoryName, @NotNull String operation, @NotNull String entityType, @Nullable String entityId, @NotNull String status, @Nullable String details);

    void logFileSystemOperation(@NotNull EventType type);
}
