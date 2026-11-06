package com.project.password.manager.cli.runtime;

import java.util.Objects;
import jakarta.validation.constraints.NotNull;

import com.google.inject.Injector;
import com.project.password.manager.cli.commands.RuntimeBoundCommand;
import com.project.password.manager.cli.handlers.CommandHandler;

public class CommandHandlerBinder {

    @NotNull
    private final Injector injector;

    @NotNull
    private final CommandHandlerRegistry handlerRegistry;

    @NotNull
    private final CommandHandlerInvoker handlerInvoker;

    public CommandHandlerBinder(@NotNull Injector injector, @NotNull CommandHandlerRegistry handlerRegistry, @NotNull CommandHandlerInvoker handlerInvoker) {
        this.injector = injector;
        this.handlerRegistry = handlerRegistry;
        this.handlerInvoker = handlerInvoker;
    }

    @SuppressWarnings({ "rawtypes" })
    public void bind(@NotNull Object command) {
        if (!(command instanceof RuntimeBoundCommand<?>)) {
            return;
        }
        Class<? extends CommandHandler<?>> handlerType = handlerRegistry.findHandlerType(command.getClass());
        if (handlerType == null) {
            throw new IllegalStateException("No handler is registered for command " + command.getClass().getSimpleName());
        }

        CommandHandler<?> handler = Objects.requireNonNull((CommandHandler<?>) injector.getInstance((Class) handlerType),
                "Guice returned null for handler: " + handlerType.getName());
        RuntimeBoundCommand runtimeBoundCommand = (RuntimeBoundCommand) command;
        runtimeBoundCommand.setHandler(handler);
        runtimeBoundCommand.setHandlerInvoker(handlerInvoker);
    }
}
