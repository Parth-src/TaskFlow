package com.project.taskflow.dashboard;

import com.project.taskflow.auth.UserContext;
import com.project.taskflow.dashboard.dto.ExecutionSummaryDTO;
import com.project.taskflow.dashboard.dto.TaskExecutionDTO;
import com.project.taskflow.execution.ExecutionStore;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            WorkerRegistry workerRegistry,
            ExecutionStore executionStore) {

        this.dashboardService =
                new DashboardService(
                        workerRegistry,
                        executionStore
                );
    }

    @GetMapping("/workers")
    public List<WorkerMetadata> getWorkers() {

        UserContext.require();

        return dashboardService.getWorkers();
    }

    @GetMapping("/executions/{executionId}")
    public List<TaskExecutionDTO> getExecution(
            @PathVariable String executionId) {

        UUID projectId =
                UserContext.require();

        return dashboardService.getExecution(
                projectId,
                executionId
        );
    }

    @GetMapping("/executions")
    public List<ExecutionSummaryDTO> getRecentExecutions(
            @RequestParam(defaultValue = "20") int limit) {

        UUID projectId =
                UserContext.require();

        return dashboardService.getRecentExecutions(
                projectId,
                limit
        );
    }
}