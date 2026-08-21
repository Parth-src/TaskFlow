package com.project.taskflow.execution;

import com.project.taskflow.enums.TaskStatus;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import java.util.ArrayList;
import java.util.List;

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
}