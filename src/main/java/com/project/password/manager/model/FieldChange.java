package com.project.password.manager.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldChange {

	@NotNull
	private final String fieldName;
	@Nullable
	private final Object oldValue;
	@NotNull
	private final Object newValue;

	public FieldChange(@NotNull String fieldName, @Nullable Object oldValue, @NotNull Object newValue) {
		this.fieldName = fieldName;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@NotNull
	public String getFieldName() {
		return fieldName;
	}

	@Nullable
	public Object getOldValue() {
		return oldValue;
	}

	@NotNull
	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		return "FieldChange{" + "fieldName='" + fieldName + '\'' + ", oldValue=" + oldValue + ", newValue=" + newValue
				+ '}';
	}
}