package com.project.taskflow.execution;

import java.time.Instant;
import java.util.UUID;

public class TaskExecution {

    private final UUID taskId;

    private final String workflowId;

    private final String executionId;

    private final String workerId;

    private String status;

    private int attempt;

    private Instant createdAt;

    private Instant startedAt;

    private Instant completedAt;

    private String error;

    public TaskExecution(
            UUID taskId,
            String workflowId,
            String executionId,
            String workerId) {

        this.taskId = taskId;
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.workerId = workerId;

        this.status = "PENDING";
        this.attempt = 0;
        this.createdAt = Instant.now();
    }

    public TaskExecution(
            UUID taskId,
            String workflowId,
            String executionId,
            String workerId,
            String status,
            int attempt,
            Instant createdAt,
            Instant startedAt,
            Instant completedAt,
            String error) {

        this.taskId = taskId;
        this.workflowId = workflowId;
        this.executionId = executionId;
        this.workerId = workerId;

        this.status = status;
        this.attempt = attempt;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.error = error;
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

    public void markRunning() {

        this.status = "RUNNING";
        this.attempt++;
        this.startedAt = Instant.now();
    }

    public void markCompleted() {

        this.status = "COMPLETED";
        this.completedAt = Instant.now();
    }

    public void markFailed(String error) {

        this.status = "FAILED";
        this.error = error;
        this.completedAt = Instant.now();
    }
}