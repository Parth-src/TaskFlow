package com.project.taskflow.project;

import com.project.taskflow.auth.UserContext;

import com.project.taskflow.project.dto.ProjectDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectAuthorizationService authorizationService;

    public ProjectController(
            ProjectService projectService,
            ProjectAuthorizationService authorizationService) {

        this.projectService =
                projectService;

        this.authorizationService =
                authorizationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDTO createProject(
            @RequestBody CreateProjectRequest request) {

        UUID userId =
                UserContext.require();

        Project project =
                projectService.createProject(
                        userId,
                        request.name()
                );

        return new ProjectDTO(project);
    }

    @GetMapping
    public List<ProjectDTO> getProjects() {

        UUID userId =
                UserContext.require();

        return projectService
                .getProjects(userId)
                .stream()
                .map(ProjectDTO::new)
                .toList();
    }

    @GetMapping("/{projectId}")
    public ProjectDTO getProject(
            @PathVariable UUID projectId) {

        UUID userId =
                UserContext.require();

        Project project =
                authorizationService.getOwnedProject(
                        projectId,
                        userId
                );

        return new ProjectDTO(project);
    }

    public record CreateProjectRequest(
            String name
    ) {
    }
}