package com.project.password.manager.service;

import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.project.password.manager.auth.token.SessionTokenRequest;
import com.project.password.manager.auth.token.SessionTokenStrategy;
import com.project.password.manager.auth.token.SessionTokenStrategyRegistry;
import com.project.password.manager.configuration.AuthenticationType;
import com.project.password.manager.database.DataRepository;
import com.project.password.manager.guice.PlatformEntityProvider;
import com.project.password.manager.model.IToken;
import com.project.password.manager.model.IUser;
import com.project.password.manager.util.ValidationUtils;

public class TokenService {

	private final Cache<String, String> tokenCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100).build();
	@NotNull
	private final DataRepository<IToken, String> tokenRepo;
	@NotNull
	private final SessionTokenStrategyRegistry sessionTokenStrategyRegistry;

	public TokenService(@NotNull SessionTokenStrategyRegistry sessionTokenStrategyRegistry,
			@NotNull DataRepository<IToken, String> tokenRepo) {
		this.sessionTokenStrategyRegistry = sessionTokenStrategyRegistry;
		this.tokenRepo = tokenRepo;
	}

	@NotNull
	public String issueToken(@NotNull IUser user, @NotNull SessionTokenRequest request) {
		SessionTokenStrategy strategy = sessionTokenStrategyRegistry.require(request.getAuthenticationType());
		IToken persistedToken = tokenRepo.findById(user.getId());
		if (persistedToken != null
				&& request.getAuthenticationType().value().equalsIgnoreCase(persistedToken.getTokenType())
				&& strategy.canReuse(persistedToken, user)) {
			tokenCache.put(user.getId(), persistedToken.getToken());
			return persistedToken.getToken();
		}
		String issuedToken = strategy.issueToken(user, request);
		persistToken(user, issuedToken, strategy.getAuthenticationType());
		return issuedToken;
	}

	@Nullable
	public String getToken(@NotNull IUser user) {
		String userId = user.getId();
		String cachedToken = tokenCache.getIfPresent(userId);
		if (cachedToken != null) {
			return cachedToken;
		}
		IToken persistedToken = tokenRepo.findById(userId);
		if (persistedToken == null) {
			return null;
		}
		String token = persistedToken.getToken();
		if (token != null) {
			tokenCache.put(userId, token);
		}
		return token;
	}

	@NotNull
	public String requireToken(@NotNull IUser user) {
		try {
			return ValidationUtils.requireText(getToken(user), "No token is available for user: " + user.getId());
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException(exception.getMessage(), exception);
		}
	}

	public boolean isCurrentSessionTokenValid(@NotNull IUser user, @NotNull String rawToken) {
		IToken persistedToken = tokenRepo.findById(user.getId());
		if (persistedToken == null || !ValidationUtils.hasText(persistedToken.getToken())
				|| !rawToken.equals(persistedToken.getToken())
				|| !ValidationUtils.hasText(persistedToken.getTokenType())) {
			return false;
		}
		AuthenticationType authenticationType = AuthenticationType.fromValue(persistedToken.getTokenType());
		if (authenticationType == null) {
			return false;
		}
		SessionTokenStrategy strategy = sessionTokenStrategyRegistry.find(authenticationType);
		if (strategy == null) {
			return false;
		}
		return strategy.isValid(persistedToken, user);
	}

	public void revokeToken(@NotNull IUser user) {
		String userId = user.getId();
		tokenCache.invalidate(userId);
		if (tokenRepo.findById(userId) != null) {
			tokenRepo.delete(userId);
		}
	}

	private void persistToken(@NotNull IUser user, @NotNull String tokenValue,
			@NotNull AuthenticationType authenticationType) {
		IToken tokenEntity = PlatformEntityProvider.getEntityProvider().getToken();
		tokenEntity.setUserId(user.getId());
		tokenEntity.setToken(tokenValue);
		tokenEntity.setTokenType(authenticationType.value());
		tokenCache.put(user.getId(), tokenValue);
		if (tokenRepo.findById(user.getId()) == null) {
			tokenRepo.save(tokenEntity);
			return;
		}
		tokenRepo.update(user.getId(), tokenEntity);
	}

}
