package com.project.taskflow.monitoring;

public class WorkerStatus {

    private final String workerId;
    private final String endpoint;
    private final boolean healthy;

    public WorkerStatus(
            String workerId,
            String endpoint,
            boolean healthy) {

        this.workerId = workerId;
        this.endpoint = endpoint;
        this.healthy = healthy;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isHealthy() {
        return healthy;
    }
}