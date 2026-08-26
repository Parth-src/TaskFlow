package com.project.taskflow.project;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Project not found"
                        )
                );
    }

    public Project getOwnedProject(
            UUID projectId,
            UUID userId) {

        Project project =
                getProject(projectId);

        verifyOwnership(
                project,
                userId
        );

        return project;
    }

    public void verifyOwnership(
            Project project,
            UUID userId) {

        if (!project.getUser()
                .getId()
                .equals(userId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this project"
            );
        }
    }
}