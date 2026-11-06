/*
 * Copyright 2026 Polarion AG
 */
package com.project.password.manager.event.listener;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public interface IPersistenceListenerSubscriberManager {

    @NotNull
    Collection<IEventListener> getSubscribedListeners();

    void addIfAbsent(@NotNull IEventListener listener);

    void addIfAbsent(@NotNull List<IEventListener> listeners);

}
