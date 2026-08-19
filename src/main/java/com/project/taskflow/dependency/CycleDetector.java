package com.project.taskflow.dependency;

import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import java.util.HashSet;
import java.util.Set;

public class CycleDetector {

    public static boolean hasCycle(
            Workflow workflow) {

        Set<WorkflowNode> visited =
                new HashSet<>();

        Set<WorkflowNode> recursionStack =
                new HashSet<>();

        for (WorkflowNode node :
                workflow.getNodes()) {

            if (detect(
                    node,
                    visited,
                    recursionStack)) {

                return true;
            }
        }

        return false;
    }

    private static boolean detect(
            WorkflowNode node,
            Set<WorkflowNode> visited,
            Set<WorkflowNode> recursionStack) {

        if (recursionStack.contains(node)) {

            return true;
        }

        if (visited.contains(node)) {

            return false;
        }

        visited.add(node);

        recursionStack.add(node);

        for (WorkflowNode child :
                node.getDependents()) {

            if (detect(
                    child,
                    visited,
                    recursionStack)) {

                return true;
            }
        }

        recursionStack.remove(node);

        return false;
    }
}