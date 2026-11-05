package com.project.password.manager.auth.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.workspace.Workspace;

public class KeyLoader {

	private static final String AUTHENTICATION_FOLDER = "authentication";

	private KeyLoader() {
		//
	}

	@NotNull
	public static RSAPrivateKey loadRsaPrivateKey(@NotNull String path) throws Exception {
		byte[] decoded = Base64.getDecoder().decode(readKey(path));
		return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
	}

	@NotNull
	public static RSAPublicKey loadRsaPublicKey(@NotNull String path) throws Exception {
		byte[] decoded = Base64.getDecoder().decode(readKey(path));
		return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
	}

	@NotNull
	public static ECPublicKey loadEcPublicKey(@NotNull String path) throws Exception {
		byte[] decoded = Base64.getDecoder().decode(readKey(path));
		return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(decoded));
	}

	@NotNull
	public static ECPrivateKey loadEcPrivateKey(@NotNull String path) throws Exception {
		byte[] decoded = Base64.getDecoder().decode(readKey(path));
		return (ECPrivateKey) KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(decoded));
	}

	@NotNull
	private static String readKey(@NotNull String fileName) throws Exception {
		Workspace ws = Workspace.getInstance();
		File authDir = new File(ws.getRoot(), AUTHENTICATION_FOLDER);
		if (!authDir.exists()) {
			throw new IllegalStateException(
					"Authentication directory not found at: " + authDir.getAbsolutePath() +
					"\nSelected algorithm requires key files." +
					"\nCreate the folder and add the required key files."
					);
		}

		File keyFile = new File(authDir, fileName);
		if (!keyFile.exists()) {
			throw new IllegalStateException(
					"Key file not found: " + keyFile.getAbsolutePath() +
					"\nSelected JWT algorithm requires this key file." +
					"\nPlease add the correct key file inside the authentication folder."
					);
		}

		try (InputStream is = new FileInputStream(keyFile)) {
			String key = new String(is.readAllBytes(), StandardCharsets.UTF_8);
			return key
					.replaceAll("-----BEGIN (.*)-----", "")
					.replaceAll("-----END (.*)-----", "")
					.replaceAll("\\s", "");
		}
	}

}
