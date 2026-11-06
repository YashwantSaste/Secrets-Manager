package com.project.password.manager.cli.runtime;

import org.jetbrains.annotations.Nullable;

import com.project.password.manager.exceptions.UnauthorizedSessionException;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

public class CliErrorHandler implements CommandLine.IExecutionExceptionHandler {

    @Override
    public int handleExecutionException(@Nullable Exception ex, @Nullable CommandLine commandLine, @Nullable ParseResult parseResult) {
        Throwable error = ex instanceof RuntimeException && ex.getCause() != null ? ex.getCause() : ex;
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            message = error.getClass().getSimpleName();
        }
        if (error instanceof UnauthorizedSessionException) {
            commandLine.getErr().println(CliTheme.errorPanel("Authorization Error",
                    CliTheme.key("message") + CliTheme.muted(" : ") + CliTheme.secondary(message),
                    CliTheme.key("action") + CliTheme.muted(" : ") + CliTheme.accent("login <username> <password>")));
        } else {
            commandLine.getErr().println(CliTheme.errorPanel("Command Failed",
                    CliTheme.key("message") + CliTheme.muted(" : ") + CliTheme.secondary(message)));
        }
        return commandLine.getCommandSpec().exitCodeOnExecutionException();
    }
}
