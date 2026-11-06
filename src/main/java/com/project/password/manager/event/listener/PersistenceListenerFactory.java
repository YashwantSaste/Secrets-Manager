package com.project.password.manager.event.listener;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.event.EventType;
import com.project.password.manager.model.IEntity;

/** Creates persistence listeners wired to the application event listeners. */
public class PersistenceListenerFactory {

    private final IEventListenerProvider listenerProvider;

    public PersistenceListenerFactory(@NotNull IEventListenerProvider listenerProvider) {
        this.listenerProvider = listenerProvider;
    }

    @NotNull
    public <T extends IEntity> IPersistenceEventListener<T> create(@NotNull T entity, @NotNull EventType type) {
        PersistenceListener<T> listener = new PersistenceListener<>(entity, type);
        IEventListener eventListener = listenerProvider.getListener(entity, type, EventLoggingListener.class);
        listener.subscribeListener(eventListener);
        return listener;
    }
}
