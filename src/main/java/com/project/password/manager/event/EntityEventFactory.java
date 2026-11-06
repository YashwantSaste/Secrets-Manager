package com.project.password.manager.event;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.model.FieldChange;
import com.project.password.manager.model.IEntity;

public class EntityEventFactory {

	@NotNull
	private final EntityChangeDetector changeDetector;

	public EntityEventFactory(@NotNull EntityChangeDetector changeDetector) {
		this.changeDetector = changeDetector;
	}

	@NotNull
	public IEvent created(@NotNull IEntity entity) {
		return new BaseEvent(EventType.CREATE, entity.getClass().getSimpleName(), resolveEntityId(entity), null, entity,
				Collections.emptyList(), Instant.now());
	}

	@NotNull
	public IEvent updated(@NotNull IEntity before, @NotNull IEntity after) {
		List<FieldChange> changes = changeDetector.detectChanges(before, after);
		return new BaseEvent(EventType.UPDATE, after.getClass().getSimpleName(), resolveEntityId(after), before, after,
				changes, Instant.now());
	}

	@NotNull
	public IEvent deleted(@NotNull IEntity entity) {
		return new BaseEvent(EventType.DELETE, entity.getClass().getSimpleName(), resolveEntityId(entity), entity, null,
				Collections.emptyList(), Instant.now());
	}

	@Nullable
	private String resolveEntityId(@NotNull IEntity entity) {
		try {
			Object id = entity.getClass().getMethod("getId").invoke(entity);
			return id == null ? null : String.valueOf(id);
		} catch (ReflectiveOperationException exception) {
			return null;
		}
	}
}