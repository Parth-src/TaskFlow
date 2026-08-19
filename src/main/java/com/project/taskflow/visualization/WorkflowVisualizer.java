package com.project.taskflow.visualization;

import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import java.util.HashSet;
import java.util.Set;

public class WorkflowVisualizer {

    public static void print(Workflow workflow) {

        Set<WorkflowNode> visited =
                new HashSet<>();

        for (WorkflowNode node : workflow.getNodes()) {

            if (node.getDependencies().isEmpty()) {

                dfs(node, visited, 0);
            }
        }
    }

    private static void dfs(
            WorkflowNode node,
            Set<WorkflowNode> visited,
            int level) {

        if (visited.contains(node)) {

            return;
        }

        visited.add(node);

        System.out.println(
                "    ".repeat(level)
                        + node.getWorkerId());

        for (WorkflowNode child :
                node.getDependents()) {

            dfs(
                    child,
                    visited,
                    level + 1);
        }
    }
}
