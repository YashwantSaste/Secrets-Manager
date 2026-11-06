package com.project.password.manager.logging;

import java.time.Instant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransactionLogEntry {

	@NotNull
	private final Instant timestamp;
	@NotNull
	private final String category;
	@NotNull
	private final String source;
	@NotNull
	private final String action;
	@NotNull
	private final String subjectType;
	@Nullable
	private final String subjectId;
	@NotNull
	private final String status;
	@Nullable
	private final String details;

	public TransactionLogEntry(@NotNull Instant timestamp, @NotNull String category, @NotNull String source,
			@NotNull String action, @NotNull String subjectType, @Nullable String subjectId, @NotNull String status,
			@Nullable String details) {
		this.timestamp = timestamp;
		this.category = category;
		this.source = source;
		this.action = action;
		this.subjectType = subjectType;
		this.subjectId = subjectId;
		this.status = status;
		this.details = details;
	}

	@NotNull
	public String toLine() {
		String normalizedDetails = details == null ? "" : details.replace(System.lineSeparator(), " ");
		String normalizedSubjectId = subjectId == null ? "-" : subjectId;
		return timestamp + " | category=" + category + " | source=" + source + " | action=" + action
				+ " | subject=" + subjectType + " | subjectId=" + normalizedSubjectId + " | status=" + status
				+ " | details=" + normalizedDetails;
	}
}