package com.project.password.manager.event;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.model.FieldChange;
import com.project.password.manager.model.IEntity;

public class BaseEvent implements IEvent {

	@NotNull
	private final EventType type;
	@NotNull
	private final String entityType;
	@Nullable
	private final String entityId;
	@Nullable
	private final IEntity before;
	@Nullable
	private final IEntity after;
	@NotNull
	private final List<FieldChange> changes;
	@NotNull
	private final Instant occurredAt;

	public BaseEvent(@NotNull EventType type, @NotNull String entityType, @Nullable String entityId,
			@Nullable IEntity before, @Nullable IEntity after, @NotNull List<FieldChange> changes,
			@NotNull Instant occurredAt) {
		this.type = type;
		this.entityType = entityType;
		this.entityId = entityId;
		this.before = before;
		this.after = after;
		this.changes = Collections.unmodifiableList(changes);
		this.occurredAt = occurredAt;
	}

	@Override
	public EventType type() {
		return type;
	}

	@Override
	@NotNull
	public String entityType() {
		return entityType;
	}

	@Override
	@Nullable
	public String entityId() {
		return entityId;
	}

	@Override
	@Nullable
	public IEntity before() {
		return before;
	}

	@Override
	@Nullable
	public IEntity after() {
		return after;
	}

	@Override
	@NotNull
	public List<FieldChange> changes() {
		return changes;
	}

	@Override
	@NotNull
	public Instant occurredAt() {
		return occurredAt;
	}

	@Override
	public String toString() {
		return "BaseEvent{" + "type=" + type + ", entityType='" + entityType + '\'' + ", entityId='" + entityId
				+ '\'' + ", before=" + Objects.toString(before) + ", after=" + Objects.toString(after)
				+ ", changes=" + changes + ", occurredAt=" + occurredAt + '}';
	}

}
