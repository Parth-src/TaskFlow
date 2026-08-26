package com.project.taskflow.demo;

import com.project.taskflow.config.TaskFlowConfig;
import com.project.taskflow.config.TaskFlowConfigLoader;
import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.dlq.InMemoryDeadLetterQueue;
import com.project.taskflow.execution.ExecutionEngine;
import com.project.taskflow.execution.WorkflowExecutor;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.retry.RetryPolicy;
import com.project.taskflow.worker.WorkerRegistry;
import com.project.taskflow.workflow.WorkflowLoader;
import com.project.taskflow.execution.ExecutionStore;
import com.project.taskflow.execution.RedisExecutionStore;
import com.project.taskflow.queue.RedisTaskQueue;
import com.project.taskflow.queue.RedisTaskScheduler;
import com.project.taskflow.queue.TaskScheduler;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.file.Path;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {

        // -----------------------------
        // 1. Validate arguments
        // -----------------------------

        if (args.length < 2) {

            System.out.println(
                    "Usage: taskflow <workflow-file> <config-file>"
            );

            return;
        }

        UUID projectId =
                UUID.randomUUID();

        Path workflowPath =
                Path.of(args[0]);

        Path configPath =
                Path.of(args[1]);


        // -----------------------------
        // 2. Load TaskFlow configuration
        // -----------------------------

        TaskFlowConfigLoader configLoader =
                new TaskFlowConfigLoader();

        TaskFlowConfig config =
                configLoader.load(
                        configPath
                );


        // -----------------------------
        // 3. Load workflow
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
        // 4. Create execution engine
        // -----------------------------

        ExecutionEngine engine =
                new ExecutionEngine(
                        workflow
                );


        // -----------------------------
        // 5. Discover workers
        // -----------------------------

        WorkerRegistry registry =
                new WorkerRegistry();

        registry.discover(
                config.getWorker().getHost()
        );


        // -----------------------------
        // 6. Retry policy
        // -----------------------------

        RetryPolicy retryPolicy =
                new RetryPolicy(3);


        // -----------------------------
        // 7. Dead Letter Queue
        // -----------------------------

        DeadLetterQueue dlq =
                new InMemoryDeadLetterQueue();


        // -----------------------------
        // 8. Redis
        // -----------------------------

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
        // 9. Create executor
        // -----------------------------

        WorkflowExecutor executor =
                new WorkflowExecutor(
                        engine,
                        registry,
                        retryPolicy,
                        dlq,
                        scheduler,
                        executionStore,
                        config.getWorker().getToken(),
                        projectId
                );


        // -----------------------------
        // 10. Execute workflow
        // -----------------------------

        executor.execute();

        factory.destroy();
    }
}