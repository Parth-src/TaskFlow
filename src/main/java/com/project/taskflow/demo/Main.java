package com.project.taskflow.demo;

import com.project.taskflow.builder.WorkflowBuilder;
import com.project.taskflow.execution.ExecutionEngine;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.utils.WorkflowPrinter;
import com.project.taskflow.visualization.WorkflowVisualizer;

public class Main {

    public static void main(String[] args) {

        Workflow workflow =
                new WorkflowBuilder("ROSCA")

                        .task("validate")

                        .then("payment")

                        .parallel(
                                "email",
                                "analytics",
                                "report"
                        )

                        .then("archive")

                        .build();

        ExecutionEngine engine =
                new ExecutionEngine(workflow);

        engine.initialize();

        for (WorkflowNode node :
                engine.getReadyTasks()) {

            System.out.println(
                    node.getWorkerId());
        }
        engine.completeTask(
                engine.getReadyTasks().get(0));





       // WorkflowVisualizer.print(workflow);
    }
}