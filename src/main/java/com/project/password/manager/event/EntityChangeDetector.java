package com.project.password.manager.event;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.password.manager.model.FieldChange;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.util.ModelObjectMapperFactory;

public class EntityChangeDetector {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @NotNull
    private final ObjectMapper objectMapper;

    public EntityChangeDetector(@NotNull ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @NotNull
    public static EntityChangeDetector getDefault() {
        return new EntityChangeDetector(ModelObjectMapperFactory.create());
    }

    @NotNull
    public List<FieldChange> detectChanges(@Nullable IEntity before, @Nullable IEntity after) {
        Map<String, Object> beforeMap = toMap(before);
        Map<String, Object> afterMap = toMap(after);
        Set<String> fieldNames = new LinkedHashSet<>();
        fieldNames.addAll(beforeMap.keySet());
        fieldNames.addAll(afterMap.keySet());

        List<FieldChange> changes = new ArrayList<>();
        for (String fieldName : fieldNames) {
            Object oldValue = beforeMap.get(fieldName);
            Object newValue = afterMap.get(fieldName);
            if (!Objects.equals(oldValue, newValue)) {
                changes.add(new FieldChange(fieldName, oldValue, newValue));
            }
        }
        return changes;
    }

    @NotNull
    private Map<String, Object> toMap(@Nullable IEntity entity) {
        if (entity == null) {
            return Map.of();
        }
        return objectMapper.convertValue(entity, MAP_TYPE);
    }
}
