package com.project.password.manager.encryption;

import javax.crypto.SecretKey;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.model.IUser;
import com.project.password.manager.service.UserService;
import com.project.password.manager.util.AesGcmUtils;
import com.project.password.manager.util.KeyGenerator;

public class AesGcmEncryptionService implements IEncryptionService {

    private final UserService userService;

    public AesGcmEncryptionService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @NotNull
    public String encrypt(@NotNull String plainText, @NotNull String userId) throws Exception {
        SecretKey key = deriveKeyForUser(getUser(userId));
        return AesGcmUtils.encrypt(key, plainText);
    }

    @Override
    @NotNull
    public String decrypt(@NotNull String encryptedText, @NotNull String userId) throws Exception {
        SecretKey key = deriveKeyForUser(getUser(userId));
        return AesGcmUtils.decrypt(key, encryptedText);
    }

    @NotNull
    private SecretKey deriveKeyForUser(@NotNull IUser user) {
        return KeyGenerator.getSecretKeyFromUserKeySalt(user.getKeySalt());
    }

    @NotNull
    private IUser getUser(@NotNull String userId) {
        return userService.requireUser(userId);
    }
}
