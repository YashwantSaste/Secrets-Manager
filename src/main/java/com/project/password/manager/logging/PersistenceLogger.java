/*
 * Copyright 2026 Polarion AG
 */
package com.project.password.manager.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.event.EventType;
import com.project.password.manager.event.IEvent;

public class PersistenceLogger implements IPersistenceLogger {

    @Override
    public void logEvent(@NotNull IEvent event, @NotNull String level) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void logRepositoryOperation(boolean databaseOperation, @NotNull String repositoryName, @NotNull String operation, @NotNull String entityType, @Nullable String entityId, @NotNull String status, @Nullable String details) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void logFileSystemOperation(@NotNull EventType type) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
