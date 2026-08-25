package com.project.taskflow.auth;

import java.util.UUID;

public final class UserContext {

    private static final ThreadLocal<UUID> CURRENT =
            new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(
            UUID userId) {

        CURRENT.set(userId);
    }

    public static UUID get() {

        return CURRENT.get();
    }

    public static UUID require() {

        UUID userId =
                CURRENT.get();

        if (userId == null) {

            throw new IllegalStateException(
                    "No authenticated user"
            );
        }

        return userId;
    }

    public static void clear() {

        CURRENT.remove();
    }
}