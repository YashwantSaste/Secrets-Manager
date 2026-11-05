package com.project.password.manager.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.configuration.IAESConfiguration;
import com.project.password.manager.configuration.IConfiguration;
import com.project.password.manager.configuration.ISaltKeyConfiguration;
import com.project.password.manager.configuration.application.Configuration;
import com.project.password.manager.exceptions.ApplicationErrorException;

public class KeyGenerator {

    static IConfiguration configuration = Configuration.getInstance();
    static ISaltKeyConfiguration saltKeyConfiguration = configuration.saltKeyConfiguration();
    static IAESConfiguration aesConfiguration = configuration.aesConfiguration();
    static SecureRandom random = new SecureRandom();

    private KeyGenerator() {
        //Derive keys for the encryption and decryption of data
    }

    @NotNull
    public static SecretKey generateKeyFromPassword(@NotNull String password) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), getSaltForKeyGeneration(), saltKeyConfiguration.iterations(), saltKeyConfiguration.keyLength());
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
            return secret;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new ApplicationErrorException("Error while generating secret key for the user: " + ex);
        }
    }

    @NotNull
    public static SecretKey getSecretKeyFromUserKeySalt(@NotNull String keySalt) {
        byte[] decodedKey = Base64.getDecoder().decode(keySalt);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    public static byte[] generateIv() {
        return getSaltForKeyGeneration();
    }

    private static byte[] getSaltForKeyGeneration() {
        byte[] salt = new byte[aesConfiguration.ivLength()];
        random.nextBytes(salt);
        return salt;
    }
}
