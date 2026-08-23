package com.project.taskflow.workflow;

import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkflowDefinitionBuilder {

    public Workflow build(
            WorkflowDefinition definition) {

        Workflow workflow =
                new Workflow(
                        definition.getName()
                );

        Map<String, WorkflowNode> nodes =
                new HashMap<>();


        // ==========================================
        // Create all nodes first
        // ==========================================

        for (TaskDefinition task :
                definition.getTasks()) {

            /*
             * Generate a deterministic UUID from:
             *
             * workflow name + task ID
             *
             * The same workflow/task combination
             * will therefore always receive the
             * same UUID across application restarts.
             */
            UUID taskId =
                    UUID.nameUUIDFromBytes(
                            (
                                    definition.getName()
                                            + ":"
                                            + task.getId()
                            ).getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            WorkflowNode node =
                    new WorkflowNode(
                            taskId,
                            task.getWorker()
                    );

            nodes.put(
                    task.getId(),
                    node
            );

            workflow.addNode(node);
        }


        // ==========================================
        // Connect dependencies
        // ==========================================

        for (TaskDefinition task :
                definition.getTasks()) {

            WorkflowNode current =
                    nodes.get(
                            task.getId()
                    );

            if (task.getDependsOn() == null) {

                continue;
            }

            for (String dependencyId :
                    task.getDependsOn()) {

                WorkflowNode dependency =
                        nodes.get(
                                dependencyId
                        );

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