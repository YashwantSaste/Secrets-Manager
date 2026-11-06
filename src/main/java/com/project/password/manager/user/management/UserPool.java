package com.project.password.manager.user.management;

import java.util.List;

import javax.annotation.Nullable;

import com.project.password.manager.auth.session.ISession;
import com.project.password.manager.cache.UserCache;
import com.project.password.manager.guice.GuicePlatform;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IUserGroup;
import com.project.password.manager.model.UserRole;
import com.project.password.manager.service.UserGroupService;
import com.project.password.manager.service.UserService;
import com.project.password.manager.validation.utlis.UserValidationUtils;

import jakarta.validation.constraints.NotNull;

public class UserPool implements IUserPool {

    @NotNull
    private UserService userService;
    @NotNull
    private UserGroupService userGroupService;

    @NotNull
    private UserCache cache = new UserCache();

    public UserPool(@NotNull UserService userService, @NotNull UserGroupService userGroupService) {
	this.userService = userService;
	this.userGroupService = userGroupService;
	loadUsersInCache();
    }

    @Override
    @NotNull
    public List<IUser> getAdminUsers() {
	return loadUsersFromCache().stream().filter(user -> user.getRoles().contains(UserRole.ADMIN)).distinct()
		.toList();
    }

    @Override
    @NotNull
    public List<IUser> getNonAdminUsers() {
	return loadUsersFromCache().stream().filter(user -> !user.getRoles().contains(UserRole.ADMIN)).distinct()
		.toList();
    }

    @Override
    @NotNull
    public List<IUser> getAllUsers() {
	return userService.getUsers();
    }

    private List<IUser> loadUsersFromCache() {
	return cache.getCachedUsers();
    }

    private void loadUsersInCache() {
	cache.addUsersToCache(getAllUsers());
    }

    @Override
    @Nullable
    public IUser getUser(@NotNull String userId) {
	IUser cachedUser = cache.getUserFromCache(userId);
	if (cachedUser != null) {
	    return cachedUser;
	}
	return userService.getUser(userId);
    }

    @Override
    @Nullable
    public IUser getCurrentLoggedInUser() {
	ISession session = GuicePlatform.getInstance(ISession.class);
	return userService.getUser(session.getCurrentSession().userId());
    }

    @Override
    @NotNull
    public List<IUserGroup> getGroupsAssoicatedWithUser(@NotNull String userId) {
	UserValidationUtils.requireUser(userId);
	return userGroupService.getUserAssociatedGroups(userId);
    }
}
