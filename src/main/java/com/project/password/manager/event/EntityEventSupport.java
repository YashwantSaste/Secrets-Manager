package com.project.password.manager.event;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.model.IEntity;

public class EntityEventSupport implements IEntityEventSupport {

	@NotNull
	private final EntitySnapshotter snapshotter;
	@NotNull
	private final EntityEventFactory eventFactory;
	@NotNull
	private final IEventPublisher eventPublisher;

	public EntityEventSupport(@NotNull EntitySnapshotter snapshotter, @NotNull EntityEventFactory eventFactory,
			@NotNull IEventPublisher eventPublisher) {
		this.snapshotter = snapshotter;
		this.eventFactory = eventFactory;
		this.eventPublisher = eventPublisher;
	}

	@Override
	@NotNull
	public <T extends IEntity> T snapshot(@NotNull T entity) {
		return snapshotter.snapshot(entity);
	}

	@Override
	public void publishCreated(@NotNull IEntity entity) {
		eventPublisher.publish(eventFactory.created(entity));
	}

	@Override
	public void publishUpdated(@NotNull IEntity before, @NotNull IEntity after) {
		eventPublisher.publish(eventFactory.updated(before, after));
	}

	@Override
	public void publishDeleted(@NotNull IEntity entity) {
		eventPublisher.publish(eventFactory.deleted(entity));
	}
}