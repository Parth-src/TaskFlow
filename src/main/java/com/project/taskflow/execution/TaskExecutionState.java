package com.project.taskflow.execution;

public class TaskExecutionState {

    private int attemptCount;

    private String lastError;

    public TaskExecutionState() {

        this.attemptCount = 0;
    }

    public void incrementAttempt() {

        attemptCount++;
    }

    public int getAttemptCount() {

        return attemptCount;
    }

    public void setLastError(
            String lastError) {

        this.lastError = lastError;
    }

    public String getLastError() {

        return lastError;
    }
}