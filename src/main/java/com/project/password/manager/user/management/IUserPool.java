package com.project.password.manager.user.management;

import java.util.List;

import javax.annotation.Nullable;

import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IUserGroup;

import jakarta.validation.constraints.NotNull;

public interface IUserPool {

    @NotNull
    List<IUser> getAdminUsers();

    @NotNull
    List<IUser> getNonAdminUsers();

    @NotNull
    List<IUser> getAllUsers();

    @Nullable
    IUser getUser(@NotNull String userId);

    @NotNull
    IUser getCurrentLoggedInUser();

    @NotNull
    List<IUserGroup> getGroupsAssoicatedWithUser(@NotNull String userId);
}
