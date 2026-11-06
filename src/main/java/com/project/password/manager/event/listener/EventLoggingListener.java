package com.project.password.manager.event.listener;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.event.EventLogger;
import com.project.password.manager.event.EventType;
import com.project.password.manager.event.IEvent;

public class EventLoggingListener implements IEventListener {

	@NotNull
	private final EventLogger eventLogger;

	public EventLoggingListener(@NotNull EventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	@Override
	public boolean supports(@NotNull IEvent event) {
		return true;
	}

	@Override
	public void onEvent(@NotNull IEvent event) {
		if (event.type() == EventType.DELETE) {
			eventLogger.logReceivedEventAsWarn(event);
			return;
		}
		eventLogger.logReceivedEventAsInfo(event);
	}
}