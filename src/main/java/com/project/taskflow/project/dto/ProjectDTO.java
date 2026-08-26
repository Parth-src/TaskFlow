package com.project.taskflow.project.dto;

import com.project.taskflow.project.Project;

import java.time.Instant;
import java.util.UUID;

public class ProjectDTO {

    private final UUID id;
    private final String name;
    private final String githubRepository;
    private final Instant createdAt;

    public ProjectDTO(Project project) {

        this.id =
                project.getId();

        this.name =
                project.getName();

        this.githubRepository =
                project.getGithubRepository();

        this.createdAt =
                project.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGithubRepository() {
        return githubRepository;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}