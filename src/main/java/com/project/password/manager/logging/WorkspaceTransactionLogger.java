package com.project.password.manager.logging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.configuration.IAppConfiguration;
import com.project.password.manager.event.IEvent;
import com.project.password.manager.util.Logger;

public class WorkspaceTransactionLogger implements ITransactionLogger {

	private static final Logger log = Logger.getLogger(WorkspaceTransactionLogger.class);

	@NotNull
	private final IAppConfiguration appConfiguration;
	@NotNull
	private final File workspaceRoot;

	public WorkspaceTransactionLogger(@NotNull IAppConfiguration appConfiguration, @NotNull File workspaceRoot) {
		this.appConfiguration = appConfiguration;
		this.workspaceRoot = workspaceRoot;
	}

	@Override
	public void logEvent(@NotNull IEvent event, @NotNull String level) {
		writeEntry(new TransactionLogEntry(Instant.now(), "event", event.entityType(), event.type().name(),
				event.entityType(), event.entityId(), level,
				"changes=" + event.changes().size() + "; occurredAt=" + event.occurredAt()));
	}

	@Override
	public void logRepositoryOperation(boolean databaseOperation, @NotNull String repositoryName,
			@NotNull String operation, @NotNull String entityType, @Nullable String entityId, @NotNull String status,
			@Nullable String details) {
		if (databaseOperation && !appConfiguration.includeDatabaseTransactions()) {
			return;
		}
		writeEntry(new TransactionLogEntry(Instant.now(), databaseOperation ? "database" : "repository",
				repositoryName, operation, entityType, entityId, status, details));
	}

	private void writeEntry(@NotNull TransactionLogEntry entry) {
		if (!appConfiguration.transactionLoggingEnabled()) {
			return;
		}
		Path logPath = resolveLogPath();
		try {
			Path parent = logPath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(logPath, entry.toLine() + System.lineSeparator(), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException exception) {
			log.error("Failed to write transaction log entry to workspace", exception);
		}
	}

	@NotNull
	private Path resolveLogPath() {
		return new File(workspaceRoot, appConfiguration.transactionLogPath()).toPath();
	}
}