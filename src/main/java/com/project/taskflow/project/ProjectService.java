package com.project.taskflow.project;

import com.project.taskflow.user.User;
import com.project.taskflow.user.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            UserRepository userRepository) {

        this.projectRepository =
                projectRepository;

        this.userRepository =
                userRepository;
    }

    public Project createProject(
            UUID userId,
            String name) {

        if (name == null ||
                name.isBlank()) {

            throw new IllegalArgumentException(
                    "Project name is required"
            );
        }

        name = name.trim();

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found"
                                )
                        );

        Project project =
                new Project(
                        user,
                        name
                );

        return projectRepository.save(
                project
        );
    }

    public List<Project> getProjects(
            UUID userId) {

        return projectRepository
                .findByUserId(userId);
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
}