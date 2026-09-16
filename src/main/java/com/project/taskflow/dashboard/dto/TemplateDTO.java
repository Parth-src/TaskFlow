package com.project.taskflow.dashboard.dto;

import java.util.List;

public class TemplateDTO {

    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final int taskCount;
    private final List<WorkflowTaskDTO> tasks;

    public TemplateDTO(
            String id,
            String name,
            String description,
            String category,
            List<WorkflowTaskDTO> tasks) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.tasks = tasks != null ? tasks : List.of();
        this.taskCount = this.tasks.size();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public List<WorkflowTaskDTO> getTasks() {
        return tasks;
    }
}
