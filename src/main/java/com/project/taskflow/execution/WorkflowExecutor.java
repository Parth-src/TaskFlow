package com.project.taskflow.execution;

import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.worker.HttpDispatcher;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;
import com.project.taskflow.worker.WorkerResponse;
import com.project.taskflow.retry.RetryPolicy;
import com.project.taskflow.dlq.DeadLetterQueue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.List;

public class WorkflowExecutor {

    private final ExecutionEngine engine;

    private final WorkerRegistry registry;

    private final HttpDispatcher dispatcher;

    private final RetryPolicy retryPolicy;

    private final DeadLetterQueue dlq;

    private final Map<String, TaskExecutionState>
            executionStates = new ConcurrentHashMap<>();

    public WorkflowExecutor(
            ExecutionEngine engine,
            WorkerRegistry registry,
            RetryPolicy retryPolicy,
            DeadLetterQueue dlq) {

        this.engine = engine;

        this.registry = registry;

        this.retryPolicy = retryPolicy;

        this.dlq = dlq;

        this.dispatcher =
                new HttpDispatcher();
    }

    public void execute() {

        engine.initialize();

        while (true) {

            List<WorkflowNode> readyTasks =
                    engine.getReadyTasks();

            if (readyTasks.isEmpty()) {
                break;
            }

            try (var executor =
                         Executors.newVirtualThreadPerTaskExecutor()) {

                List<Future<?>> futures =
                        new ArrayList<>();

                for (WorkflowNode node : readyTasks) {

                    futures.add(

                            executor.submit(() -> {

                                TaskExecutionState state =
                                        executionStates.computeIfAbsent(
                                                node.getWorkerId(),
                                                key -> new TaskExecutionState()
                                        );

                                while (true) {

                                    state.incrementAttempt();

                                    System.out.println(
                                            "Executing "
                                                    + node.getWorkerId()
                                                    + " | Attempt "
                                                    + state.getAttemptCount()
                                    );

                                    WorkerMetadata worker =
                                            registry.get(
                                                    node.getWorkerId()
                                            );

                                    if (worker == null) {

                                        state.setLastError(
                                                "Worker not found"
                                        );

                                        System.out.println(
                                                "Worker not found: "
                                                        + node.getWorkerId()
                                        );

                                        break;
                                    }

                                    WorkerResponse response =
                                            dispatcher.dispatch(worker);

                                    if (response.isSuccess()) {

                                        synchronized (engine) {

                                            engine.completeTask(node);
                                        }

                                        break;
                                    }

                                    state.setLastError(
                                            response.getMessage()
                                    );

                                    if (!response.shouldRetry()) {

                                        System.out.println("Task failed permanently: " + node.getWorkerId());

                                        synchronized (engine) {

                                            engine.failTask(node);

                                            dlq.enqueue(node, state, response.getMessage());
                                        }

                                        break;
                                    }

                                    if (state.getAttemptCount()
                                            >= retryPolicy.getMaxRetries()) {

                                        System.out.println(
                                                "Retry limit reached for: "
                                                        + node.getWorkerId()
                                        );

                                        synchronized (engine) {

                                            engine.failTask(node);
                                            dlq.enqueue(
                                                    node,
                                                    state,
                                                    response.getMessage()
                                            );
                                        }

                                        break;
                                    }

                                    System.out.println(
                                            "Retrying: "
                                                    + node.getWorkerId()
                                    );
                                }
                            })
                    );
                }

                for (Future<?> future : futures) {

                    future.get();
                }

            } catch (Exception e) {

                throw new RuntimeException(e);
            }
        }

        System.out.println(
                "Workflow completed."
        );
    }

    public void reprocess(UUID taskId) {

        DeadLetterEntry entry =
                dlq.get(taskId);

        if (entry == null) {

            System.out.println(
                    "Task not found in DLQ: "
                            + taskId
            );

            return;
        }

        WorkflowNode node =
                entry.getNode();

        WorkerMetadata worker =
                registry.get(
                        node.getWorkerId()
                );

        if (worker == null) {

            System.out.println(
                    "Worker not found: "
                            + node.getWorkerId()
            );

            return;
        }

        System.out.println(
                "Reprocessing task: "
                        + node.getWorkerId()
        );

        WorkerResponse response =
                dispatcher.dispatch(worker);

        if (response.isSuccess()) {

            synchronized (engine) {

                engine.completeTask(node);
            }

            dlq.remove(taskId);

            System.out.println(
                    "Task successfully reprocessed."
            );

        } else {

            System.out.println(
                    "Reprocessing failed: "
                            + response.getMessage()
            );
        }
    }

}