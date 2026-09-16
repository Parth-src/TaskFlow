package com.project.taskflow.dashboard.dto;

public class OverviewDTO {

    private final int totalProjects;
    private final int totalWorkflows;
    private final int runningExecutions;
    private final int completedExecutions;
    private final int failedExecutions;
    private final int queuedTasks;
    private final int activeWorkers;
    private final int dlqCount;

    public OverviewDTO(
            int totalProjects,
            int totalWorkflows,
            int runningExecutions,
            int completedExecutions,
            int failedExecutions,
            int queuedTasks,
            int activeWorkers,
            int dlqCount) {

        this.totalProjects = totalProjects;
        this.totalWorkflows = totalWorkflows;
        this.runningExecutions = runningExecutions;
        this.completedExecutions = completedExecutions;
        this.failedExecutions = failedExecutions;
        this.queuedTasks = queuedTasks;
        this.activeWorkers = activeWorkers;
        this.dlqCount = dlqCount;
    }

    public int getTotalProjects() {
        return totalProjects;
    }

    public int getTotalWorkflows() {
        return totalWorkflows;
    }

    public int getRunningExecutions() {
        return runningExecutions;
    }

    public int getCompletedExecutions() {
        return completedExecutions;
    }

    public int getFailedExecutions() {
        return failedExecutions;
    }

    public int getQueuedTasks() {
        return queuedTasks;
    }

    public int getActiveWorkers() {
        return activeWorkers;
    }

    public int getDlqCount() {
        return dlqCount;
    }
}
