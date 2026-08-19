package com.project.taskflow.worker;

public class WorkerMetadata {

    private final String workerId;

    private final String endpoint;

    public WorkerMetadata(
            String workerId,
            String endpoint) {

        this.workerId = workerId;
        this.endpoint = endpoint;
    }

    public String getWorkerId() {

        return workerId;
    }

    public String getEndpoint() {

        return endpoint;
    }
}
