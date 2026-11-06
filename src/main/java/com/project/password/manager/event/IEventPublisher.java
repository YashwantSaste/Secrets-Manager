package com.project.password.manager.event;

import org.jetbrains.annotations.NotNull;

public interface IEventPublisher {

	void publish(@NotNull IEvent event);
}