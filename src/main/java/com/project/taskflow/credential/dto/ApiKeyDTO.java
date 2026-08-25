package com.project.taskflow.credential.dto;

import com.project.taskflow.credential.CredentialEnvironment;
import com.project.taskflow.credential.ProjectCredential;

import java.time.Instant;
import java.util.UUID;

public class ApiKeyDTO {

    private final UUID id;
    private final String name;
    private final CredentialEnvironment environment;
    private final Instant createdAt;
    private final Instant lastUsedAt;
    private final Instant revokedAt;

    public ApiKeyDTO(
            ProjectCredential credential) {

        this.id =
                credential.getId();

        this.name =
                credential.getName();

        this.environment =
                credential.getEnvironment();

        this.createdAt =
                credential.getCreatedAt();

        this.lastUsedAt =
                credential.getLastUsedAt();

        this.revokedAt =
                credential.getRevokedAt();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public CredentialEnvironment getEnvironment() {
        return environment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}