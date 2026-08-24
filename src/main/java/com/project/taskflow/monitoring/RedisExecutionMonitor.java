package com.project.taskflow.monitoring;

import com.project.taskflow.execution.RedisExecutionStore;
import com.project.taskflow.execution.TaskExecution;

import java.util.List;
import java.util.UUID;

public class RedisExecutionMonitor
        implements ExecutionMonitor {

    private final RedisExecutionStore store;

    public RedisExecutionMonitor(
            RedisExecutionStore store) {

        this.store = store;
    }

    @Override
    public TaskExecution getTask(
            String executionId,
            UUID taskId) {

        return store.get(
                executionId,
                taskId
        );
    }

    @Override
    public List<TaskExecution> getWorkflowTasks(
            String executionId) {

        return store.getByExecutionId(
                executionId
        );
    }
}