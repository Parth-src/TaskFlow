package com.project.taskflow.dlq;

import com.project.taskflow.model.WorkflowNode;

import java.time.Instant;
import java.util.UUID;

public class DeadLetterEntry {

    private final UUID taskId;

    private final String workerId;

    private final int attemptCount;

    private final String reason;

    private final Instant timestamp;

    private final WorkflowNode node;


    /*
     * Used when creating a new DLQ entry.
     */
    public DeadLetterEntry(
            WorkflowNode node,
            int attemptCount,
            String reason) {

        this(
                node,
                attemptCount,
                reason,
                Instant.now()
        );
    }


    /*
     * Used when reconstructing an existing
     * DLQ entry from storage.
     */
    public DeadLetterEntry(
            WorkflowNode node,
            int attemptCount,
            String reason,
            Instant timestamp) {

        this.node = node;

        this.taskId =
                node.getId();

        this.workerId =
                node.getWorkerId();

        this.attemptCount =
                attemptCount;

        this.reason =
                reason;

        this.timestamp =
                timestamp;
    }


    public UUID getTaskId() {
        return taskId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public String getReason() {
        return reason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public WorkflowNode getNode() {
        return node;
    }
}