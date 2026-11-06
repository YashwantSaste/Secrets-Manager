package com.project.password.manager.event.listener;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.event.EventType;
import com.project.password.manager.event.IEvent;
import com.project.password.manager.model.IEntity;

public interface IEventListenerProvider {

    @NotNull
    <T extends IEventListener> T getListener(@NotNull IEvent event);

    @NotNull
    <T extends IEventListener> T getListener(@NotNull IEvent event, @NotNull Class<? extends IEventListener> listenerClass);

    @NotNull
    <T extends IEventListener> T getListener(@NotNull IEntity entity, @NotNull EventType type);

    @NotNull
    <T extends IEventListener> T getListener(@NotNull IEntity entity, @NotNull EventType type, @NotNull Class<? extends IEventListener> listenerClass);
}
