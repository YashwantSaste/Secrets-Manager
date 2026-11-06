package com.project.password.manager.event.listener;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.event.EventType;
import com.project.password.manager.event.IEvent;
import com.project.password.manager.model.IEntity;

public interface IPersistenceEventListener<T extends IEntity> extends IEventListener {

    @NotNull
    EventType getEventType();

    @NotNull
    IPersistenceListenerSubscriberManager getManager();

    T getEntity();

    /*
     *
     */
    void onUpdate(@NotNull T after);

    @NotNull
    IEvent createEvent();

    List<IEventListener> getSubscribedListeners();

    void subscribeListener(@NotNull IEventListener listener);

    void subscribeListener(@NotNull List<IEventListener> listeners);

}
