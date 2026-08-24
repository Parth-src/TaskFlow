package com.project.taskflow.dashboard.dto;

public class WorkerDTO {

    private final String workerId;

    private final String endpoint;

    public WorkerDTO(
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