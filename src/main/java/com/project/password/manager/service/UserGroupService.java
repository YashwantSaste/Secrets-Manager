package com.project.password.manager.service;

import java.util.ArrayList;
import java.util.List;

import com.project.password.manager.database.DataRepository;
import com.project.password.manager.event.IEntityEventSupport;
import com.project.password.manager.exceptions.EntityNotFoundException;
import com.project.password.manager.guice.Platform;
import com.project.password.manager.model.IUser;
import com.project.password.manager.model.IUserGroup;
import com.project.password.manager.util.ValidationUtils;
import com.project.password.manager.validation.utlis.UserValidationUtils;

import jakarta.validation.constraints.NotNull;

public class UserGroupService {

    @NotNull
    private final DataRepository<IUserGroup, String> userGroupRepository;
    @NotNull
    private final DataRepository<IUser, String> userRepository;
    @NotNull
    private final IEntityEventSupport eventSupport;

    public UserGroupService(@NotNull DataRepository<IUserGroup, String> userGroupRepository,
	    @NotNull DataRepository<IUser, String> userRepository, @NotNull IEntityEventSupport eventSupport) {
	this.userGroupRepository = userGroupRepository;
	this.userRepository = userRepository;
	this.eventSupport = eventSupport;
    }

    @NotNull
    public IUserGroup createGroup(@NotNull String groupId, @NotNull String groupName, @NotNull List<String> userIds) {
	String normalizedGroupId = ValidationUtils.requireText(groupId, "Group id is required.");
	String normalizedGroupName = ValidationUtils.requireText(groupName, "Group name is required.");
	if (userGroupRepository.findById(normalizedGroupId) != null) {
	    throw new IllegalArgumentException("User group already exists: " + normalizedGroupId);
	}
	for (String userId : userIds) {
	    UserValidationUtils.requireUser(userRepository, userId);
	}
	IUserGroup group = Platform.getPlatformContext().getUserGroup();
	group.setId(normalizedGroupId);
	group.setName(normalizedGroupName);
	group.setUsers(distinct(userIds));
	userGroupRepository.save(group);
	eventSupport.publishCreated(group);
	return group;
    }

    @NotNull
    public IUserGroup getGroup(@NotNull String groupId) {
	IUserGroup group = userGroupRepository.findById(groupId);
	if (group == null) {
	    throw new EntityNotFoundException("User group not found: " + groupId);
	}
	return group;
    }

    @NotNull
    public List<IUserGroup> getGroups() {
	return userGroupRepository.findAll();
    }

    @NotNull
    public IUserGroup addUser(@NotNull String groupId, @NotNull String userId) {
	UserValidationUtils.requireUser(userRepository, userId);
	IUserGroup group = getGroup(groupId);
	IUserGroup before = eventSupport.snapshot(group);
	if (!group.getUsers().contains(userId)) {
	    group.getUsers().add(userId);
	    userGroupRepository.update(groupId, group);
	    eventSupport.publishUpdated(before, group);
	}
	return group;
    }

    @NotNull
    public IUserGroup removeUser(@NotNull String groupId, @NotNull String userId) {
	IUserGroup group = getGroup(groupId);
	IUserGroup before = eventSupport.snapshot(group);
	if (group.getUsers().remove(userId)) {
	    userGroupRepository.update(groupId, group);
	    eventSupport.publishUpdated(before, group);
	}
	return group;
    }

    public void deleteGroup(@NotNull String groupId) {
	IUserGroup group = getGroup(groupId);
	userGroupRepository.delete(groupId);
	eventSupport.publishDeleted(group);
    }

    public List<IUserGroup> getUserAssociatedGroups(@NotNull String userId) {
	return getGroups().stream().filter(group -> group.getUsers().contains(userId)).toList();
    }

    @NotNull
    private List<String> distinct(@NotNull List<String> values) {
	List<String> result = new ArrayList<>();
	for (String value : values) {
	    if (!result.contains(value)) {
		result.add(value);
	    }
	}
	return result;
    }
}
