package com.project.taskflow.dashboard.dto;

import com.project.taskflow.execution.TaskExecution;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class TaskExecutionDTO {

    private final UUID taskId;
    private final String workflowId;
    private final String executionId;
    private final String workerId;
    private final String status;
    private final int attempt;
    private final Instant createdAt;
    private final Instant startedAt;
    private final Instant completedAt;
    private final String error;
    private final Long durationMs;

    public TaskExecutionDTO(
            TaskExecution execution) {

        this.taskId =
                execution.getTaskId();

        this.workflowId =
                execution.getWorkflowId();

        this.executionId =
                execution.getExecutionId();

        this.workerId =
                execution.getWorkerId();

        this.status =
                execution.getStatus();

        this.attempt =
                execution.getAttempt();

        this.createdAt =
                execution.getCreatedAt();

        this.startedAt =
                execution.getStartedAt();

        this.completedAt =
                execution.getCompletedAt();

        this.error =
                execution.getError();

        // Calculate task execution duration
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

    public UUID getTaskId() {
        return taskId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempt() {
        return attempt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getError() {
        return error;
    }

    public Long getDurationMs() {
        return durationMs;
    }
}