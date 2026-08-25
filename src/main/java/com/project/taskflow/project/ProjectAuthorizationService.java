package com.project.taskflow.project;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProjectAuthorizationService {

    private final ProjectRepository projectRepository;

    public ProjectAuthorizationService(
            ProjectRepository projectRepository) {

        this.projectRepository =
                projectRepository;
    }

    public Project getProject(
            UUID projectId) {

        return projectRepository
                .findById(projectId)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Project not found"
                        )
                );
    }

    public void verifyOwnership(
            Project project,
            UUID userId) {

        if (!project.getUser()
                .getId()
                .equals(userId)) {

            throw new RuntimeException(
                    "User does not own this project"
            );
        }
    }
}