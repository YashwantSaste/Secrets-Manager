package com.project.password.manager.event;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.password.manager.model.IEntity;

public class EntitySnapshotter {

    @NotNull
    private final ObjectMapper objectMapper;

    public EntitySnapshotter(@NotNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @NotNull
    public <T extends IEntity> T snapshot(@NotNull T entity) {
        return (T) objectMapper.convertValue(entity, entity.getClass());
    }
}
