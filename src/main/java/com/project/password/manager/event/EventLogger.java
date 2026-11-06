package com.project.password.manager.event;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.configuration.application.Configuration;
import com.project.password.manager.configuration.application.Workspace;
import com.project.password.manager.logging.ITransactionLogger;
import com.project.password.manager.logging.WorkspaceTransactionLogger;
import com.project.password.manager.util.Logger;

public class EventLogger {

	private static final Logger log = Logger.getLogger(EventLogger.class);
	@NotNull
	private final ITransactionLogger transactionLogger;

	public EventLogger() {
		this(new WorkspaceTransactionLogger(Configuration.getInstance().appConfiguration(),
				Workspace.getInstance().getRoot()));
	}

	public EventLogger(@NotNull ITransactionLogger transactionLogger) {
		this.transactionLogger = transactionLogger;
	}

	public void logReceivedEventAsDebug(@NotNull IEvent event) {
		if (log.isDebugEnabled()) {
			log.debug(event);
		}
		transactionLogger.logEvent(event, "DEBUG");
	}

	public void logReceivedEventAsInfo(@NotNull IEvent event) {
		log.info(event);
		transactionLogger.logEvent(event, "INFO");
	}

	public void logReceivedEventAsWarn(@NotNull IEvent event) {
		log.warn(event);
		transactionLogger.logEvent(event, "WARN");
	}

	public void logReceivedEventAsError(@NotNull IEvent event) {
		log.error(event);
		transactionLogger.logEvent(event, "ERROR");
	}
}