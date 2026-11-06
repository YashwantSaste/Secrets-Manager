package com.project.password.manager.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.argon.Argon2Encoder;
import com.project.password.manager.auth.token.SessionTokenRequest;
import com.project.password.manager.guice.PlatformEntityProvider;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.UserRole;
import com.project.password.manager.util.KeyGenerator;
import com.project.password.manager.workspace.Workspace;

public class AuthService {

	@NotNull
	private final TokenService tokenService;
	@NotNull
	private final UserService userService;
	@NotNull
	private final VaultService vaultService;
	@NotNull
	private final Argon2Encoder encoder;

	public AuthService(@NotNull TokenService tokenService, @NotNull UserService userService,
			@NotNull Argon2Encoder encoder, @NotNull VaultService vaultService) {
		this.tokenService=tokenService;
		this.userService=userService;
		this.encoder=encoder;
		this.vaultService = vaultService;
	}

	public void login(@NotNull String username,@NotNull String password) {
		IUser user;
		try {
			user = userService.requireUser(username);
		} catch (IllegalArgumentException exception) {
			throw new RuntimeException("Given user does not exist", exception);
		}
		if(!encoder.verify(user.getAuthVerifier(),password)) {
			throw new RuntimeException("Password mismatch. Kindly check your password");
		}
		tokenService.issueToken(user, SessionTokenRequest.jwt());
	}

	public void signup(@NotNull String username, @NotNull String password) {
		if (userService.getUser(username) != null) {
			throw new RuntimeException("User already exists");
		}
		IUser newUser = PlatformEntityProvider.getEntityProvider().getUser();
		newUser.setId(username);
		newUser.setName(username);
		newUser.setAuthVerifier(encoder.getHashValue(password));
		byte[] saltBytes = KeyGenerator.generateKeyFromPassword(password).getEncoded();
		String keySalt = Base64.getEncoder().encodeToString(saltBytes);
		newUser.setKeySalt(keySalt);
		newUser.setVaults(new ArrayList<>());
		newUser.setRoles(determineInitialRoles());
		userService.saveUser(newUser);
		vaultService.createDefaultVault(newUser);
		tokenService.issueToken(newUser, SessionTokenRequest.jwt());
	}

	@NotNull
	public IUser loginWithOAuth2Profile(@NotNull String userId, @NotNull String displayName,
			@NotNull String providerAccessToken) {
		IUser user = userService.getUser(userId);
		if (user == null) {
			user = createOAuth2User(userId, displayName);
		}
		tokenService.issueToken(user, SessionTokenRequest.oauth2(providerAccessToken));
		return user;
	}

	@NotNull
	private IUser createOAuth2User(@NotNull String userId, @NotNull String displayName) {
		String generatedSecret = UUID.randomUUID().toString();
		IUser oauthUser = PlatformEntityProvider.getEntityProvider().getUser();
		oauthUser.setId(userId);
		oauthUser.setName(displayName);
		oauthUser.setAuthVerifier(encoder.getHashValue(generatedSecret));
		byte[] saltBytes = KeyGenerator.generateKeyFromPassword(generatedSecret).getEncoded();
		oauthUser.setKeySalt(Base64.getEncoder().encodeToString(saltBytes));
		oauthUser.setVaults(new ArrayList<>());
		oauthUser.setRoles(determineInitialRoles());
		userService.saveUser(oauthUser);
		vaultService.createDefaultVault(oauthUser);
		return oauthUser;
	}

	@NotNull
	private List<UserRole> determineInitialRoles() {
		List<IUser> user = userService.getUsers();
		if (user.isEmpty() || user == null) {
			return List.of(UserRole.ADMIN, UserRole.USER);
		}
		return List.of(UserRole.USER);
	}
}
