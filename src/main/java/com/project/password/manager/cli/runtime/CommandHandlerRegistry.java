package com.project.password.manager.cli.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import com.project.password.manager.cli.commands.RuntimeBoundCommand;
import com.project.password.manager.cli.handlers.CommandHandler;

public class CommandHandlerRegistry {

    @NotNull
    private final Map<Class<?>, Class<? extends CommandHandler<?>>> handlerTypeByCommand = new LinkedHashMap<>();

    @NotNull
    public <C extends RuntimeBoundCommand<?>, H extends CommandHandler<?>> CommandHandlerRegistry register(@NotNull Class<C> commandType, @NotNull Class<H> handlerType) {
        handlerTypeByCommand.put(commandType, handlerType);
        return this;
    }

    @Nullable
    public Class<? extends CommandHandler<?>> findHandlerType(@NotNull Class<?> commandType) {
        return handlerTypeByCommand.get(commandType);
    }
}
