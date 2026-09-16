package com.project.taskflow.dashboard.dto;

import java.util.List;

public class WorkflowSummaryDTO {

    private final String id;
    private final String name;
    private final int taskCount;
    private final List<WorkflowTaskDTO> tasks;

    public WorkflowSummaryDTO(
            String id,
            String name,
            List<WorkflowTaskDTO> tasks) {

        this.id = id;
        this.name = name;
        this.tasks = tasks != null ? tasks : List.of();
        this.taskCount = this.tasks.size();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public List<WorkflowTaskDTO> getTasks() {
        return tasks;
    }
}
