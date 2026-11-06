package com.project.password.manager.model;

import org.jetbrains.annotations.NotNull;

public interface IEntity {
	// marker interface extending all the entities;
	@NotNull
	Class<?> entityClassType();
}
