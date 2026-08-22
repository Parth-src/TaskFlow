package com.project.taskflow.workflow;

import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import java.util.HashMap;
import java.util.Map;

public class WorkflowDefinitionBuilder {

    public Workflow build(
            WorkflowDefinition definition) {

        Workflow workflow =
                new Workflow(
                        definition.getName()
                );

        Map<String, WorkflowNode> nodes =
                new HashMap<>();

        // Create all nodes first
        for (TaskDefinition task :
                definition.getTasks()) {

            WorkflowNode node =
                    new WorkflowNode(
                            task.getWorker()
                    );

            nodes.put(
                    task.getId(),
                    node
            );

            workflow.addNode(node);
        }

        // Connect dependencies
        for (TaskDefinition task :
                definition.getTasks()) {

            WorkflowNode current =
                    nodes.get(task.getId());

            if (task.getDependsOn() == null) {
                continue;
            }

            for (String dependencyId :
                    task.getDependsOn()) {

                WorkflowNode dependency =
                        nodes.get(dependencyId);

                if (dependency == null) {

                    throw new IllegalArgumentException(
                            "Unknown dependency: "
                                    + dependencyId
                    );
                }

                current.addDependency(
                        dependency
                );

                dependency.addDependent(
                        current
                );
            }
        }

        return workflow;
    }
}