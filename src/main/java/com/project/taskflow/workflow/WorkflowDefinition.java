package com.project.taskflow.workflow;

import java.util.List;

public class WorkflowDefinition {

    private String name;

    private List<TaskDefinition> tasks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TaskDefinition> getTasks() {
        return tasks;
    }

    public void setTasks(
            List<TaskDefinition> tasks) {

        this.tasks = tasks;
    }
}