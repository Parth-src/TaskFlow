package com.project.taskflow.cli;

import com.project.taskflow.config.TaskFlowConfig;
import com.project.taskflow.config.TaskFlowConfigLoader;
import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.dlq.InMemoryDeadLetterQueue;
import com.project.taskflow.execution.ExecutionEngine;
import com.project.taskflow.execution.ExecutionStore;
import com.project.taskflow.execution.RedisExecutionStore;
import com.project.taskflow.execution.WorkflowExecutor;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.retry.RetryPolicy;
import com.project.taskflow.worker.WorkerRegistry;
import com.project.taskflow.workflow.WorkflowLoader;
import com.project.taskflow.queue.RedisTaskQueue;
import com.project.taskflow.queue.RedisTaskScheduler;
import com.project.taskflow.queue.TaskScheduler;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.file.Path;

public class TaskFlowCLI {

    public static void main(String[] args) {

        DeadLetterQueue dlq =
                new InMemoryDeadLetterQueue();

        LettuceConnectionFactory factory =
                new LettuceConnectionFactory(
                        "localhost",
                        6379
                );

        factory.afterPropertiesSet();

        StringRedisTemplate redis =
                new StringRedisTemplate(factory);

        RedisTaskQueue queue =
                new RedisTaskQueue(redis);

        TaskScheduler scheduler =
                new RedisTaskScheduler(queue);

        ExecutionStore executionStore =
                new RedisExecutionStore(redis);

        // -----------------------------
        // 1. Validate command
        // -----------------------------

        if (args.length == 0) {

            printUsage();

            return;
        }

        if (!args[0].equals("run")) {

            System.out.println(
                    "Unknown command: "
                            + args[0]
            );

            printUsage();

            return;
        }


        // -----------------------------
        // 2. Validate arguments
        // -----------------------------

        if (args.length < 4 ||
                !args[2].equals("--config")) {

            printUsage();

            return;
        }

        Path workflowPath =
                Path.of(args[1]);

        Path configPath =
                Path.of(args[3]);


        // -----------------------------
        // 3. Load configuration
        // -----------------------------

        TaskFlowConfigLoader configLoader =
                new TaskFlowConfigLoader();

        TaskFlowConfig config =
                configLoader.load(
                        configPath
                );


        // -----------------------------
        // 4. Load workflow
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
        // 5. Create execution engine
        // -----------------------------

        ExecutionEngine engine =
                new ExecutionEngine(
                        workflow
                );


        // -----------------------------
        // 6. Discover workers
        // -----------------------------

        WorkerRegistry registry =
                new WorkerRegistry();

        registry.discover(
                config.getWorker().getHost()
        );


        // -----------------------------
        // 7. Retry policy
        // -----------------------------

        RetryPolicy retryPolicy =
                new RetryPolicy(3);


        // -----------------------------
        // 8. Dead Letter Queue
        // -----------------------------

       // DeadLetterQueue dlq =
           //     new InMemoryDeadLetterQueue();


        // -----------------------------
        // 9. Create executor
        // -----------------------------

        WorkflowExecutor executor =
                new WorkflowExecutor(
                        engine,
                        registry,
                        retryPolicy,
                        dlq,
                        scheduler,
                        executionStore
                );


        // -----------------------------
        // 10. Execute
        // -----------------------------

        executor.execute();

        factory.destroy();
    }


    private static void printUsage() {

        System.out.println(
                "Usage:"
        );

        System.out.println(
                "  taskflow run <workflow-file> --config <config-file>"
        );
    }
}