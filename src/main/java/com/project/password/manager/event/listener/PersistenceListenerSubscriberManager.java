package com.project.password.manager.event.listener;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PersistenceListenerSubscriberManager implements IPersistenceListenerSubscriberManager {

    @NotNull
    private static final Set<IEventListener> subscribedListeners = ConcurrentHashMap.newKeySet();

    @Inject
    public PersistenceListenerSubscriberManager() {
        // For Guice singleton binding
    }

    @Override
    @NotNull
    public Collection<IEventListener> getSubscribedListeners() {
        return Set.copyOf(subscribedListeners);
    }

    @Override
    public void addIfAbsent(@NotNull IEventListener listener) {
        subscribedListeners.add(listener);
    }

    @Override
    public void addIfAbsent(@NotNull List<IEventListener> listeners) {
        subscribedListeners.addAll(listeners);
    }
}
