package com.project.taskflow.execution;

import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.queue.TaskScheduler;
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

    private final TaskScheduler scheduler;

    private final ExecutionStore executionStore;


    private final Map<UUID, TaskExecutionState>
            executionStates = new ConcurrentHashMap<>();

    public WorkflowExecutor(
            ExecutionEngine engine,
            WorkerRegistry registry,
            RetryPolicy retryPolicy,
            DeadLetterQueue dlq,
            TaskScheduler scheduler,
            ExecutionStore executionStore) {

        this.engine = engine;
        this.registry = registry;
        this.retryPolicy = retryPolicy;
        this.dlq = dlq;
        this.scheduler = scheduler;
        this.executionStore = executionStore;

        this.dispatcher =
                new HttpDispatcher();
    }

    public void execute() {

        engine.initialize();

        engine.scheduleReadyTasks(
                scheduler
        );

        String executionId =
                UUID.randomUUID().toString();


        while (true) {

            scheduler.recoverExpired();

            List<UUID> taskIds =
                    scheduler.nextTasks(100);

            if (taskIds.isEmpty()) {
                break;
            }

            try (var executor =
                         Executors.newVirtualThreadPerTaskExecutor()) {

                List<Future<?>> futures =
                        new ArrayList<>();

                for (UUID taskId : taskIds) {

                    WorkflowNode node =
                            engine.getNode(taskId);

                    if (node == null) {

                        System.out.println(
                                "Task not found: "
                                        + taskId
                        );

                        continue;
                    }

                    TaskExecution execution =
                            new TaskExecution(
                                    taskId,
                                    engine.getWorkflowId(),
                                    executionId,
                                    node.getWorkerId()
                            );

                    executionStore.save(execution);

                    futures.add(
                            executor.submit(() -> {

                                TaskExecutionState state =
                                        executionStates.computeIfAbsent(
                                                taskId,
                                                key ->
                                                        new TaskExecutionState()
                                        );
                                while (true) {

                                    executionStore.markRunning(taskId);

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
                                            dispatcher.dispatch(
                                                    worker
                                            );

                                    if (response.isSuccess()) {

                                        executionStore.markCompleted(taskId);

                                        synchronized (engine) {

                                            engine.completeTask(node);
                                        }

                                        scheduler.complete(taskId);

                                        break;
                                    }

                                    state.setLastError(
                                            response.getMessage()
                                    );

                                    if (!response.shouldRetry()) {

                                        executionStore.markFailed(
                                                taskId,
                                                response.getMessage()
                                        );

                                        synchronized (engine) {

                                            engine.failTask(node);

                                            dlq.enqueue(
                                                    node,
                                                    state,
                                                    response.getMessage()
                                            );
                                        }

                                        scheduler.complete(taskId);

                                        break;
                                    }

                                    if (state.getAttemptCount()
                                            >= retryPolicy.getMaxRetries()) {

                                        executionStore.markFailed(
                                                taskId,
                                                response.getMessage()
                                        );

                                        synchronized (engine) {

                                            engine.failTask(node);

                                            dlq.enqueue(
                                                    node,
                                                    state,
                                                    response.getMessage()
                                            );
                                        }

                                        scheduler.complete(taskId);

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

            /*
             * Some tasks may have become READY
             * after the previous batch completed.
             */
            engine.scheduleReadyTasks(
                    scheduler
            );
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