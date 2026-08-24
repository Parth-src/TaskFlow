package com.project.taskflow.dashboard;

import com.project.taskflow.dashboard.dto.TaskExecutionDTO;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

import com.project.taskflow.execution.ExecutionStore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

        return dashboardService.getWorkers();
    }


    @GetMapping("/executions/{executionId}")
    public List<TaskExecutionDTO> getExecution(
            @PathVariable String executionId) {

        return dashboardService.getExecution(
                executionId
        );
    }

}