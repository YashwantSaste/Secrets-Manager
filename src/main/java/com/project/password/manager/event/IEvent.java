package com.project.password.manager.event;

import java.time.Instant;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.model.FieldChange;
import com.project.password.manager.model.IEntity;

public interface IEvent {

	EventType type();

	@NotNull
	String entityType();

	@Nullable
	String entityId();

	@Nullable
	IEntity before();

	@NotNull
	default IEntity eventPresentEntity() {
		IEntity afterEntity = after();
		if (afterEntity != null) {
			return afterEntity;
		}
		IEntity beforeEntity = before();
		if (beforeEntity == null) {
			throw new IllegalStateException("Events must contain at least one entity state.");
		}
		return beforeEntity;
	}

	@Nullable
	IEntity after();

	@NotNull
	List<FieldChange> changes();

	@NotNull
	Instant occurredAt();

	@Override
	String toString();
}
