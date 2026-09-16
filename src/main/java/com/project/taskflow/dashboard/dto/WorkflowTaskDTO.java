package com.project.taskflow.dashboard.dto;

import java.util.List;

public class WorkflowTaskDTO {

    private final String id;
    private final String worker;
    private final List<String> dependsOn;
    private final java.util.Map<String, Object> params;

    public WorkflowTaskDTO(
            String id,
            String worker,
            List<String> dependsOn) {

        this(id, worker, dependsOn, null);
    }

    public WorkflowTaskDTO(
            String id,
            String worker,
            List<String> dependsOn,
            java.util.Map<String, Object> params) {

        this.id = id;
        this.worker = worker;
        this.dependsOn = dependsOn != null ? dependsOn : List.of();
        this.params = params != null ? params : java.util.Map.of();
    }

    public String getId() {
        return id;
    }

    public String getWorker() {
        return worker;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public java.util.Map<String, Object> getParams() {
        return params;
    }
}
