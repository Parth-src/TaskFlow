package com.project.taskflow.dashboard.dto;

import com.project.taskflow.execution.TaskExecution;

import java.time.Duration;
import java.time.Instant;

public class ExecutionSummaryDTO {

    private final String executionId;
    private final String workflowId;
    private final String status;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Long durationMs;

    public ExecutionSummaryDTO(
            TaskExecution task) {

        this.executionId =
                task.getExecutionId();

        this.workflowId =
                task.getWorkflowId();

        this.status =
                task.getStatus();

        this.startedAt =
                task.getStartedAt();

        this.completedAt =
                task.getCompletedAt();

        if (startedAt != null &&
                completedAt != null) {

            this.durationMs =
                    Duration.between(
                            startedAt,
                            completedAt
                    ).toMillis();

        } else {

            this.durationMs = null;
        }
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }
}