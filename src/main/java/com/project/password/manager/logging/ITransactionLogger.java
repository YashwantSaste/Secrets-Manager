package com.project.password.manager.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.event.IEvent;

public interface ITransactionLogger {

	void logEvent(@NotNull IEvent event, @NotNull String level);

	void logRepositoryOperation(boolean databaseOperation, @NotNull String repositoryName, @NotNull String operation,
			@NotNull String entityType, @Nullable String entityId, @NotNull String status, @Nullable String details);
}