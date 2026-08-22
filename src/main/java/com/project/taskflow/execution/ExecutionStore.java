package com.project.taskflow.execution;

import java.util.UUID;

public interface ExecutionStore {

    void save(TaskExecution execution);

    TaskExecution get(UUID taskId);

    void markRunning(UUID taskId);

    void markCompleted(UUID taskId);

    void markFailed(
            UUID taskId,
            String reason
    );
}