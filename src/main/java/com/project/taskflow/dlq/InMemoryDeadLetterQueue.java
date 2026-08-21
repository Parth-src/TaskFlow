package com.project.taskflow.dlq;

import com.project.taskflow.execution.TaskExecutionState;
import com.project.taskflow.model.WorkflowNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InMemoryDeadLetterQueue
        implements DeadLetterQueue {

    private final List<DeadLetterEntry> failedTasks =
            new ArrayList<>();

    @Override
    public void enqueue(
            WorkflowNode node,
            TaskExecutionState state,
            String reason) {

        DeadLetterEntry entry =
                new DeadLetterEntry(
                        node,
                        state.getAttemptCount(),
                        reason
                );

        failedTasks.add(entry);

        System.out.println(
                "Task moved to DLQ: "
                        + entry.getWorkerId()
        );

        System.out.println(
                "Task ID: "
                        + entry.getTaskId()
        );

        System.out.println(
                "Attempts: "
                        + entry.getAttemptCount()
        );

        System.out.println(
                "Reason: "
                        + entry.getReason()
        );

        System.out.println(
                "Timestamp: "
                        + entry.getTimestamp()
        );
    }

    @Override
    public List<DeadLetterEntry> getEntries() {

        return List.copyOf(failedTasks);
    }

    @Override
    public DeadLetterEntry get(UUID taskId) {

        for (DeadLetterEntry entry : failedTasks) {

            if (entry.getTaskId().equals(taskId)) {

                return entry;
            }
        }

        return null;
    }

    @Override
    public void remove(UUID taskId) {

        failedTasks.removeIf(
                entry ->
                        entry.getTaskId()
                                .equals(taskId)
        );
    }
}