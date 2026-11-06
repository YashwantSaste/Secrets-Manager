/*
 * Copyright 2026 Polarion AG
 */
package com.project.password.manager.event.listener;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.project.password.manager.event.EventType;
import com.project.password.manager.event.IEvent;
import com.project.password.manager.model.IEntity;

@Singleton
public final class EventListenerProvider implements IEventListenerProvider {

    private final Set<IEventListener> listeners;

    @Inject
    public EventListenerProvider(Set<IEventListener> listeners) {
	this.listeners = listeners;
    }

    @Override
    @NotNull
    public <T extends IEventListener> T getListener(@NotNull IEvent event, @NotNull Class<? extends IEventListener> listenerClass) {
	return findListener(listenerClass);
    }

    @Override
    @NotNull
    public <T extends IEventListener> T getListener(@NotNull IEntity entity, @NotNull EventType type, @NotNull Class<? extends IEventListener> listenerClass) {
	return findListener(listenerClass);
    }

    @Override
    public <T extends IEventListener> @NotNull T getListener(@NotNull Class<T> listenerClass) {
	return findListener(listenerClass);
    }

    @SuppressWarnings("unchecked")
    private <T extends IEventListener> T findListener(@NotNull Class<? extends IEventListener> listenerClass) {
	for (IEventListener candidate : listeners) {
	    if (listenerClass.isInstance(candidate)) {
		return (T) candidate;
	    }
	}
	throw new IllegalArgumentException("No listener registered for: " + listenerClass.getName());
    }
}
