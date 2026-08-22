package com.project.taskflow.execution;

import com.project.taskflow.enums.TaskStatus;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.queue.TaskScheduler;

import java.util.*;

public class ExecutionEngine {

    private final Workflow workflow;

    public ExecutionEngine(Workflow workflow) {

        this.workflow = workflow;
    }

    public void initialize() {

        for (WorkflowNode node : workflow.getNodes()) {

            if (node.getDependencies().isEmpty()) {

                node.setStatus(TaskStatus.READY);
            }
        }
    }

    public List<WorkflowNode> getReadyTasks() {

        List<WorkflowNode> readyTasks =
                new ArrayList<>();

        for (WorkflowNode node : workflow.getNodes()) {

            if (node.getStatus() ==
                    TaskStatus.READY) {

                readyTasks.add(node);
            }
        }

        return readyTasks;
    }

    public void completeTask(
            WorkflowNode completedNode) {

        completedNode.setStatus(
                TaskStatus.COMPLETED);

        for (WorkflowNode child :
                completedNode.getDependents()) {

            boolean allDependenciesCompleted =
                    true;

            for (WorkflowNode dependency :
                    child.getDependencies()) {

                if (dependency.getStatus() !=
                        TaskStatus.COMPLETED) {

                    allDependenciesCompleted =
                            false;

                    break;
                }
            }

            if (allDependenciesCompleted) {

                child.setStatus(
                        TaskStatus.READY);
            }
        }
    }

    public void failTask(
            WorkflowNode failedNode) {

        failedNode.setStatus(
                TaskStatus.FAILED
        );
    }

    private final Set<UUID> scheduledTasks =
            new HashSet<>();

    public void scheduleReadyTasks(
            TaskScheduler scheduler) {

        List<WorkflowNode> readyTasks =
                getReadyTasks();

        for (WorkflowNode node : readyTasks) {

            UUID taskId =
                    node.getId();

            if (scheduledTasks.contains(taskId)) {
                continue;
            }

            scheduler.schedule(
                    taskId,
                    System.currentTimeMillis()
            );

            scheduledTasks.add(taskId);
        }
    }

    public WorkflowNode getNode(UUID taskId) {

        for (WorkflowNode node : workflow.getNodes()) {

            if (node.getId().equals(taskId)) {
                return node;
            }
        }

        return null;
    }

    public String getWorkflowId() {
        return workflow.getName();
    }
}
