package com.project.password.manager.cli.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.cli.handlers.CommandHandler;
import com.project.password.manager.cli.runtime.CommandHandlerInvoker;

public abstract class DelegatingCliCommand<TRequest, THandler extends CommandHandler<TRequest>> implements Runnable, RuntimeBoundCommand<THandler> {

    @Nullable
    private THandler handler;
    @Nullable
    private CommandHandlerInvoker handlerInvoker;

    @Override
    public final void run() {
        THandler currentHandler = handler;
        CommandHandlerInvoker currentInvoker = handlerInvoker;
        if (currentHandler == null || currentInvoker == null) {
            throw new IllegalStateException("Command handler is not configured for " + getClass().getSimpleName());
        }
        currentInvoker.invoke(currentHandler, buildRequest());
    }

    @Override
    public final void setHandler(@NotNull THandler handler) {
        this.handler = handler;
    }

    @Override
    public final void setHandlerInvoker(@NotNull CommandHandlerInvoker handlerInvoker) {
        this.handlerInvoker = handlerInvoker;
    }

    @NotNull
    protected abstract TRequest buildRequest();
}
