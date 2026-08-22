package com.project.taskflow.monitoring;

import com.project.taskflow.execution.TaskExecution;

import java.util.List;
import java.util.UUID;

public interface ExecutionMonitor {

    TaskExecution getTask(
            UUID taskId
    );

    List<TaskExecution> getWorkflowTasks(
            String executionId
    );
}