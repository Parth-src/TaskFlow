package com.project.taskflow.monitoring;

import com.project.taskflow.execution.TaskExecution;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitor")
public class MonitoringController {

    private final ExecutionMonitor executionMonitor;

    private final WorkerMonitor workerMonitor;

    public MonitoringController(
            ExecutionMonitor executionMonitor,
            WorkerMonitor workerMonitor) {

        this.executionMonitor =
                executionMonitor;

        this.workerMonitor =
                workerMonitor;
    }

    @GetMapping("/tasks/{taskId}")
    public TaskExecution getTask(
            @PathVariable UUID taskId) {

        return executionMonitor.getTask(
                taskId
        );
    }

    @GetMapping(
            "/executions/{executionId}"
    )
    public List<TaskExecution> getExecution(
            @PathVariable String executionId) {

        return executionMonitor.getWorkflowTasks(
                executionId
        );
    }
}