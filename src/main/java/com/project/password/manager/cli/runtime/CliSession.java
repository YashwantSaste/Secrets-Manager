package com.project.password.manager.cli.runtime;

import java.util.Objects;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.inject.Singleton;
import com.project.password.manager.exceptions.UnauthorizedSessionException;

@Singleton
public class CliSession {

    @Nullable
    private String userId;
    @Nullable
    private String token;

    public synchronized void open(@NotNull String userId, @NotNull String token) {
        this.userId = userId;
        this.token = token;
    }

    public synchronized void clear() {
        userId = null;
        token = null;
    }

    public synchronized boolean isAuthenticated() {
        return userId != null && !userId.isBlank() && token != null && !token.isBlank();
    }

    @NotNull
    public synchronized String requireUserId() {
        return Objects.requireNonNull(userId, () -> {
            throw new UnauthorizedSessionException("No active session. Please login first.");
        });
    }

    @NotNull
    public synchronized String requireToken() {
        return Objects.requireNonNull(token, () -> {
            throw new UnauthorizedSessionException("No active session. Please login first.");
        });
    }
}
