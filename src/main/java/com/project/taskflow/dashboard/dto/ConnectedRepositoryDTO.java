package com.project.taskflow.dashboard.dto;

import java.time.Instant;
import java.util.UUID;

public class ConnectedRepositoryDTO {

    private final UUID projectId;
    private final String projectName;
    private final String repository;
    private final Instant createdAt;

    public ConnectedRepositoryDTO(
            UUID projectId,
            String projectName,
            String repository,
            Instant createdAt) {

        this.projectId = projectId;
        this.projectName = projectName;
        this.repository = repository;
        this.createdAt = createdAt;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getRepository() {
        return repository;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
