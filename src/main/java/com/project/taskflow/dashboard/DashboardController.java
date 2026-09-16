package com.project.taskflow.dashboard;

import com.project.taskflow.auth.UserContext;
import com.project.taskflow.dashboard.dto.*;
import com.project.taskflow.worker.WorkerMetadata;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService) {

        this.dashboardService = dashboardService;
    }

    @GetMapping("/overview")
    public OverviewDTO getOverview(
            @RequestParam(required = false) UUID projectId) {

        UUID userId = UserContext.require();
        return dashboardService.getOverview(userId, projectId);
    }

    @GetMapping("/workers")
    public List<WorkerMetadata> getWorkers() {

        UserContext.require();
        return dashboardService.getWorkers();
    }

    @GetMapping("/executions/{executionId}")
    public List<TaskExecutionDTO> getExecution(
            @PathVariable String executionId,
            @RequestParam(required = false) UUID projectId) {

        UUID userId = UserContext.require();
        if (projectId != null) {
            return dashboardService.getExecution(projectId, userId, executionId);
        }
        return dashboardService.getExecutionForUser(userId, executionId);
    }

    @GetMapping("/executions")
    public List<ExecutionSummaryDTO> getRecentExecutions(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "20") int limit) {

        UUID userId = UserContext.require();
        if (projectId != null) {
            return dashboardService.getRecentExecutions(projectId, userId, limit);
        }
        return dashboardService.getRecentExecutionsForUser(userId, limit);
    }

    @GetMapping("/workflows")
    public List<WorkflowSummaryDTO> getWorkflows() {

        UserContext.require();
        return dashboardService.getWorkflows();
    }

    @GetMapping("/workflows/{workflowId}")
    public WorkflowSummaryDTO getWorkflow(
            @PathVariable String workflowId) {

        UserContext.require();
        return dashboardService.getWorkflow(workflowId);
    }

    @PostMapping("/workflows/{workflowId}/execute")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, Object> executeWorkflow(
            @PathVariable String workflowId,
            @RequestParam UUID projectId) {

        UUID userId = UserContext.require();
        return dashboardService.executeWorkflow(projectId, userId, workflowId);
    }

    @GetMapping("/templates")
    public List<TemplateDTO> getTemplates() {

        UserContext.require();
        return dashboardService.getTemplates();
    }

    @GetMapping("/repositories")
    public List<ConnectedRepositoryDTO> getConnectedRepositories() {

        UUID userId = UserContext.require();
        return dashboardService.getConnectedRepositories(userId);
    }

    @GetMapping("/github/repositories")
    public List<GitHubRepositoryDTO> getGitHubRepositories() {

        UUID userId = UserContext.require();
        return dashboardService.getGitHubRepositories(userId);
    }
}