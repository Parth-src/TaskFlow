package com.project.taskflow.auth;

import java.util.UUID;

public final class ProjectContext {

    private static final ThreadLocal<UUID> CURRENT =
            new ThreadLocal<>();

    private ProjectContext() {
    }

    public static void set(UUID projectId) {
        CURRENT.set(projectId);
    }

    public static UUID get() {
        return CURRENT.get();
    }

    public static UUID require() {

        UUID projectId = CURRENT.get();

        if (projectId == null) {
            throw new IllegalStateException(
                    "No authenticated TaskFlow project"
            );
        }

        return projectId;
    }

    public static void clear() {
        CURRENT.remove();
    }
}