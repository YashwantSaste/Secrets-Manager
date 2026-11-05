/*
 * Copyright 2026 Polarion AG
 */
package com.project.password.manager.exceptions;

import org.jetbrains.annotations.NotNull;

public class ApplicationErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ApplicationErrorException(@NotNull String message) {
        super(message);
    }

    public ApplicationErrorException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
