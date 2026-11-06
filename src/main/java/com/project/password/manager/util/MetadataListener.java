package com.project.password.manager.util;

import java.time.LocalDateTime;

import org.jetbrains.annotations.NotNull;

import com.project.password.manager.model.HasMetadata;
import com.project.password.manager.model.IEntity;
import com.project.password.manager.model.IMetadata;
import com.project.password.manager.model.Status;

public final class MetadataListener {

    private static final String DEFAULT_VERSION = "1.0";

    private MetadataListener() {
    }

    public static void beforeCreate(@NotNull IEntity entity) {
        touch(entity, true);
    }

    public static void beforeUpdate(@NotNull IEntity entity) {
        touch(entity, false);
    }

    private static void touch(@NotNull IEntity entity, boolean create) {
        if (!(entity instanceof HasMetadata hasMetadata)) {
            return;
        }

        IMetadata metadata = hasMetadata.metadata();
        LocalDateTime now = LocalDateTime.now();

        if (create) {
            metadata.setCreatedAt(now);
        } else {
            metadata.setUpdatedAt(now);
        }

        metadata.setLastAccessedAt(now);

        if (metadata.version().isBlank()) {
            metadata.setVersion(DEFAULT_VERSION);
        }

        metadata.setStatus(Status.ACTIVE);
        hasMetadata.setMetadata(metadata);
    }
}
