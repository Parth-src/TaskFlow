package com.project.taskflow.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TaskDefinition {

    private String id;

    private String worker;

    @JsonProperty("depends_on")
    private List<String> dependsOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public List<String> getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(
            List<String> dependsOn) {

        this.dependsOn = dependsOn;
    }
}