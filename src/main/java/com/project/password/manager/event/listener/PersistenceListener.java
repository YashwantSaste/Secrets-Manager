package com.project.password.manager.event.listener;

import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.event.EntityChangeDetector;
import com.project.password.manager.event.EntityEventFactory;
import com.project.password.manager.event.EntitySnapshotter;
import com.project.password.manager.event.EventType;
import com.project.password.manager.event.IEvent;
import com.project.password.manager.logging.IPersistenceLogger;
import com.project.password.manager.logging.PersistenceLogger;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.util.ModelObjectMapperFactory;

public class PersistenceListener<T extends IEntity> implements IPersistenceEventListener<T> {

    @NotNull
    private final T entity;
    @NotNull
    private final EventType type;
    @Nullable
    private final T beforeSnapshot;
    @NotNull
    private final EntityEventFactory eventFactory;
    @NotNull
    private final IPersistenceLogger logger = new PersistenceLogger();
    @NotNull
    private final IPersistenceListenerSubscriberManager manager = new PersistenceListenerSubscriberManager();

    public PersistenceListener(@NotNull T entity, @NotNull EventType type) {
        this(entity, type, getSnapShotter(), getFactory());
    }

    public PersistenceListener(@NotNull T entity, @NotNull EventType type, @NotNull EntitySnapshotter snapshotter, @NotNull EntityEventFactory eventFactory) {
        this.entity = entity;
        this.type = type;
        this.eventFactory = eventFactory;
        beforeSnapshot = type == EventType.UPDATE ? snapshotter.snapshot(entity) : null;
    }

    @NotNull
    protected IPersistenceLogger getLogger() {
        return logger;
    }

    @NotNull
    protected static EntityEventFactory getFactory() {
        return new EntityEventFactory(getChangeDetector());
    }

    @NotNull
    protected static EntitySnapshotter getSnapShotter() {
        return new EntitySnapshotter(ModelObjectMapperFactory.create());
    }

    @Override
    @NotNull
    public IPersistenceListenerSubscriberManager getManager() {
        return manager;
    }

    @Override
    @NotNull
    public EventType getEventType() {
        return type;
    }

    @Override
    @NotNull
    public IEvent createEvent() {
        switch (type) {
            case CREATE:
                return eventFactory.created(entity);
            case DELETE:
                return eventFactory.deleted(entity);
            default:
                throw new IllegalArgumentException("Invalid entity type");
        }
    }

    @NotNull
    protected static EntityChangeDetector getChangeDetector() {
        return EntityChangeDetector.getDefault();
    }

    @Override
    public T getEntity() {
        return entity;
    }

    @Override
    public List<IEventListener> getSubscribedListeners() {
        return List.copyOf(manager.getSubscribedListeners());
    }

    @Override
    public void subscribeListener(@NotNull IEventListener listener) {
        manager.addIfAbsent(listener);
    }

    @Override
    public void subscribeListener(@NotNull List<IEventListener> listeners) {
        manager.addIfAbsent(listeners);
    }

    @Override
    public void onUpdate(@NotNull T after) {
        onEvent(eventFactory.updated(Objects.requireNonNull(beforeSnapshot, "Before state of object needs to be non null"), after));
    }

    @Override
    public void onEvent(@NotNull IEvent event) {
        for (IEventListener listener : manager.getSubscribedListeners()) {
            listener.onEvent(event);
        }
    }

    @Override
    public boolean supports(@NotNull IEvent event) {
        return true;
    }

}
