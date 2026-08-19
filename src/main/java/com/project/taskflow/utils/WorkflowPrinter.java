package com.project.taskflow.utils;

import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import java.util.HashSet;
import java.util.Set;

public class WorkflowPrinter {

    public static void print(Workflow workflow) {

        Set<WorkflowNode> visited = new HashSet<>();

        for (WorkflowNode node : workflow.getNodes()) {

            if (node.getDependencies().isEmpty()) {

                printNode(node, visited, 0);
            }
        }
    }

    private static void printNode(
            WorkflowNode node,
            Set<WorkflowNode> visited,
            int depth) {

        if (visited.contains(node)) {
            return;
        }

        visited.add(node);

        System.out.println(
                "  ".repeat(depth)
                        + node.getWorkerId());

        for (WorkflowNode child :
                node.getDependents()) {

            printNode(
                    child,
                    visited,
                    depth + 1);
        }
    }
}
