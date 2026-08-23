package com.project.taskflow.cli;

import com.project.taskflow.config.TaskFlowConfig;
import com.project.taskflow.config.TaskFlowConfigLoader;
import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.execution.ExecutionEngine;
import com.project.taskflow.execution.ExecutionStore;
import com.project.taskflow.execution.WorkflowExecutor;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.queue.TaskScheduler;
import com.project.taskflow.retry.RetryPolicy;
import com.project.taskflow.worker.WorkerRegistry;
import com.project.taskflow.workflow.WorkflowLoader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;

@Component
public class TaskFlowCLI implements CommandLineRunner {

    private final WorkerRegistry registry;
    private final DeadLetterQueue dlq;
    private final TaskScheduler scheduler;
    private final ExecutionStore executionStore;

    public TaskFlowCLI(
            WorkerRegistry registry,
            DeadLetterQueue dlq,
            TaskScheduler scheduler,
            ExecutionStore executionStore) {

        this.registry = registry;
        this.dlq = dlq;
        this.scheduler = scheduler;
        this.executionStore = executionStore;
    }

    @Override
    public void run(String... args) {

        if (args.length == 0) {

            printUsage();

            return;
        }

        // =====================================================
        // RUN
        // =====================================================

        if (args[0].equals("run")) {

            if (args.length < 4 ||
                    !args[2].equals("--config")) {

                printUsage();

                return;
            }

            Path workflowPath =
                    Path.of(args[1]);

            Path configPath =
                    Path.of(args[3]);

            executeWorkflow(
                    workflowPath,
                    configPath
            );

            return;
        }


        // =====================================================
        // REPROCESS
        // =====================================================

        if (args[0].equals("reprocess")) {

            if (args.length < 5 ||
                    !args[3].equals("--config")) {

                printUsage();

                return;
            }

            Path workflowPath =
                    Path.of(args[1]);

            UUID taskId;

            try {

                taskId =
                        UUID.fromString(
                                args[2]
                        );

            } catch (IllegalArgumentException e) {

                System.out.println(
                        "Invalid task ID: "
                                + args[2]
                );

                return;
            }

            Path configPath =
                    Path.of(args[4]);

            reprocessTask(
                    workflowPath,
                    taskId,
                    configPath
            );

            return;
        }


        // =====================================================
        // UNKNOWN COMMAND
        // =====================================================

        System.out.println(
                "Unknown command: "
                        + args[0]
        );

        printUsage();
    }


    private void executeWorkflow(
            Path workflowPath,
            Path configPath) {

        // -----------------------------
        // Load configuration
        // -----------------------------

        TaskFlowConfigLoader configLoader =
                new TaskFlowConfigLoader();

        TaskFlowConfig config =
                configLoader.load(
                        configPath
                );


        // -----------------------------
        // Load workflow
        // -----------------------------

        WorkflowLoader workflowLoader =
                new WorkflowLoader();

        Workflow workflow =
                workflowLoader.load(
                        workflowPath
                );

        System.out.println(
                "Loaded workflow: "
                        + workflow.getName()
        );


        // -----------------------------
        // Execution engine
        // -----------------------------

        ExecutionEngine engine =
                new ExecutionEngine(
                        workflow
                );


        // -----------------------------
        // Discover workers
        // -----------------------------

        registry.discover(
                config.getWorker().getHost()
        );


        // -----------------------------
        // Retry policy
        // -----------------------------

        RetryPolicy retryPolicy =
                new RetryPolicy(3);


        // -----------------------------
        // Executor
        // -----------------------------

        WorkflowExecutor executor =
                new WorkflowExecutor(
                        engine,
                        registry,
                        retryPolicy,
                        dlq,
                        scheduler,
                        executionStore,
                        config.getWorker().getToken()
                );


        // -----------------------------
        // Execute
        // -----------------------------

        executor.execute();
    }


    private void reprocessTask(
            Path workflowPath,
            UUID taskId,
            Path configPath) {

        // -----------------------------
        // Load configuration
        // -----------------------------

        TaskFlowConfigLoader configLoader =
                new TaskFlowConfigLoader();

        TaskFlowConfig config =
                configLoader.load(
                        configPath
                );


        // -----------------------------
        // Load workflow
        // -----------------------------

        WorkflowLoader workflowLoader =
                new WorkflowLoader();

        Workflow workflow =
                workflowLoader.load(
                        workflowPath
                );


        // -----------------------------
        // Execution engine
        // -----------------------------

        ExecutionEngine engine =
                new ExecutionEngine(
                        workflow
                );


        // -----------------------------
        // Discover workers
        // -----------------------------

        registry.discover(
                config.getWorker().getHost()
        );


        // -----------------------------
        // Retry policy
        // -----------------------------

        RetryPolicy retryPolicy =
                new RetryPolicy(3);


        // -----------------------------
        // Executor
        // -----------------------------

        WorkflowExecutor executor =
                new WorkflowExecutor(
                        engine,
                        registry,
                        retryPolicy,
                        dlq,
                        scheduler,
                        executionStore,
                        config.getWorker().getToken()
                );


        // -----------------------------
        // Reprocess
        // -----------------------------

        executor.reprocess(
                taskId
        );
    }


    private void printUsage() {

        System.out.println(
                "Usage:"
        );

        System.out.println(
                "  taskflow run <workflow-file> --config <config-file>"
        );

        System.out.println(
                "  taskflow reprocess <workflow-file> <task-id> --config <config-file>"
        );
    }
}