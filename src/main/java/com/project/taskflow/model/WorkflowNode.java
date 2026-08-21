package com.project.taskflow.model;

import com.project.taskflow.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkflowNode {

    private final UUID id;

    private final String workerId;

    private final List<WorkflowNode> dependencies;

    private final List<WorkflowNode> dependents;

    private TaskStatus status;

    public WorkflowNode(String workerId) {

        this.id = UUID.randomUUID();

        this.workerId = workerId;

        this.dependencies = new ArrayList<>();

        this.dependents = new ArrayList<>();
    }

    public TaskStatus getStatus() {

        return status;
    }

    public void setStatus(
            TaskStatus status) {

        this.status = status;
    }

    public void addDependency(WorkflowNode node) {

        dependencies.add(node);
    }

    public void addDependent(WorkflowNode node) {

        dependents.add(node);
    }

    public String getWorkerId() {

        return workerId;
    }

    public List<WorkflowNode> getDependencies() {

        return dependencies;
    }

    public List<WorkflowNode> getDependents() {

        return dependents;
    }

    public UUID getId() {

        return id;
    }
}