package com.project.password.manager.model.database.nosql;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.model.IMetadata;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IVault;
import com.project.password.manager.model.UserRole;

public class UserDocument implements IUser {

    // Implementation will be added in the upcoming code

    @Override
    @NotNull
    public String getId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public String getName() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String getAuthVerifier() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String getDefaultVaultId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<IVault> getVaults() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull List<UserRole> getRoles() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull IMetadata metadata() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull String getKeySalt() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMetadata(@NotNull IMetadata metadata) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setId(@NotNull String id) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setName(@NotNull String name) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setAuthVerifier(@NotNull String authVerifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setKeySalt(@NotNull String keySalt) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDefaultVaultId(@NotNull String defaultVaultId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setVaults(@NotNull List<IVault> vaults) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setRoles(@NotNull List<UserRole> roles) {
        // TODO Auto-generated method stub

    }

}
