package com.project.password.manager.cli.runtime;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.inject.Injector;
import com.project.password.manager.exceptions.ApplicationErrorException;

import picocli.CommandLine;

public class CliCommandFactory implements CommandLine.IFactory {

    @NotNull
    private final CommandLine.IFactory delegate = CommandLine.defaultFactory();
    @NotNull
    private final CommandHandlerBinder handlerBinder;

    public CliCommandFactory(@NotNull Injector injector, @NotNull CommandHandlerRegistry handlerRegistry) {
        handlerBinder = new CommandHandlerBinder(injector, handlerRegistry, Objects.requireNonNull(injector.getInstance(CommandHandlerInvoker.class), () -> {
            throw new ApplicationErrorException("CommandHandlerInvoker instance cannot be null");
        }));
    }

    @Override
    public <T> T create(@Nullable Class<T> clazz) throws Exception {
        T command = Objects.requireNonNull(delegate.create(clazz), () -> {
            throw new ApplicationErrorException("Cannot create command for null class type");
        });
        handlerBinder.bind(command);
        return command;
    }
}
