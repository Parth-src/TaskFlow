package com.project.taskflow.demo;

import com.project.taskflow.builder.WorkflowBuilder;
import com.project.taskflow.execution.ExecutionEngine;
import com.project.taskflow.execution.WorkflowExecutor;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;

import com.project.taskflow.worker.HttpDispatcher;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

public class Main {

    public static void main(String[] args) {



        Workflow workflow =
                new WorkflowBuilder("ROSCA")

                        .task("send-email")

                        .build();

        ExecutionEngine engine =
                new ExecutionEngine(workflow);

        WorkerRegistry registry =
                new WorkerRegistry();

        registry.register(
                "send-email",
                "http://localhost:8081/workers/send-email"
        );

        WorkflowExecutor executor =
                new WorkflowExecutor(
                        engine,
                        registry
                );

        executor.execute();
    }
}