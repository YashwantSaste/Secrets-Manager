package com.project.password.manager.model;

import org.jetbrains.annotations.NotNull;

public interface IEntry extends IEntity {

	@Override
	@NotNull
	default Class<?> entityClassType() {
		return IEntry.class;
	}

	@NotNull
	String getId();

	@NotNull
	String getTitle();

	@NotNull
	ILogin getLogin();

	@NotNull
	ITag getTag();
}
