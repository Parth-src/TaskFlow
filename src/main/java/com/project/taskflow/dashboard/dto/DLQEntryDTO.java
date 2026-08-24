package com.project.taskflow.dashboard.dto;

import com.project.taskflow.dlq.DeadLetterEntry;

import java.time.Instant;
import java.util.UUID;

public class DLQEntryDTO {

    private final UUID taskId;
    private final String workerId;
    private final int attemptCount;
    private final String reason;
    private final Instant timestamp;

    public DLQEntryDTO(
            DeadLetterEntry entry) {

        this.taskId =
                entry.getTaskId();

        this.workerId =
                entry.getWorkerId();

        this.attemptCount =
                entry.getAttemptCount();

        this.reason =
                entry.getReason();

        this.timestamp =
                entry.getTimestamp();
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
}