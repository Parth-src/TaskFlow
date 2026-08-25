package com.project.taskflow.execution;

import java.util.List;
import java.util.UUID;

public interface ExecutionStore {

    void save(TaskExecution execution);

    TaskExecution get(
            String executionId,
            UUID taskId
    );

    List<TaskExecution> getByExecutionId(
            String executionId
    );

    List<TaskExecution> getRecentExecutions(
            int limit
    );

    void markRunning(
            String executionId,
            UUID taskId
    );

    void markCompleted(
            String executionId,
            UUID taskId
    );

    void markFailed(
            String executionId,
            UUID taskId,
            String reason
    );
}