package com.project.password.manager.event;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.event.listener.IEventListener;

public class EventDispatcher implements IEventPublisher {

	@NotNull
	private final List<IEventListener> listeners;

	public EventDispatcher(@NotNull List<IEventListener> listeners) {
		this.listeners = List.copyOf(listeners);
	}

	@Override
	public void publish(@NotNull IEvent event) {
		for (IEventListener listener : listeners) {
			if (listener.supports(event)) {
				listener.onEvent(event);
			}
		}
	}
}