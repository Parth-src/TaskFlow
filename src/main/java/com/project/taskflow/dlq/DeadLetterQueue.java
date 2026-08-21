package com.project.taskflow.dlq;

import com.project.taskflow.execution.TaskExecutionState;
import com.project.taskflow.model.WorkflowNode;

import java.util.List;
import java.util.UUID;

public interface DeadLetterQueue {

    void enqueue(
            WorkflowNode node,
            TaskExecutionState state,
            String reason
    );

    List<DeadLetterEntry> getEntries();

    DeadLetterEntry get(
            UUID taskId
    );

    void remove(
            UUID taskId
    );
}