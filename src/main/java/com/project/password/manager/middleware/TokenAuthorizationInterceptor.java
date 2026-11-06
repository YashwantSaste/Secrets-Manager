package com.project.password.manager.middleware;

import java.lang.reflect.Method;
import java.util.Objects;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.inject.Inject;
import com.project.password.manager.model.UserRole;

public class TokenAuthorizationInterceptor implements MethodInterceptor {

    @Nullable
    private TokenVerifier tokenVerifier;

    public TokenAuthorizationInterceptor() {
        // Guice injects TokenVerifier via requestInjection in the module configuration.
    }

    @Inject
    public void setTokenVerifier(@NotNull TokenVerifier tokenVerifier) {
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    public Object invoke(@Nullable MethodInvocation invocation) throws Throwable {
        Method method = Objects.requireNonNull(invocation).getMethod();

        @Nullable
        RequireAuthorization authorization = getAuthorizationAnnotation(method);

        UserRole[] roles = authorization == null ? new UserRole[0] : authorization.roles();
        Objects.requireNonNull(tokenVerifier).validateCurrentSession(roles);
        return invocation.proceed();
    }

    @Nullable
    private RequireAuthorization getAuthorizationAnnotation(@NotNull Method method) {
        return method.getAnnotation(RequireAuthorization.class);
    }
}
