package com.project.taskflow.execution;

import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.queue.TaskScheduler;
import com.project.taskflow.retry.RetryPolicy;
import com.project.taskflow.worker.HttpDispatcher;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;
import com.project.taskflow.worker.WorkerResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WorkflowExecutor {

    private final ExecutionEngine engine;

    private final WorkerRegistry registry;

    private final HttpDispatcher dispatcher;

    private final RetryPolicy retryPolicy;

    private final DeadLetterQueue dlq;

    private final TaskScheduler scheduler;

    private final ExecutionStore executionStore;

    private final UUID projectId;

    private final Map<UUID, TaskExecutionState>
            executionStates =
            new ConcurrentHashMap<>();


    public WorkflowExecutor(
            ExecutionEngine engine,
            WorkerRegistry registry,
            RetryPolicy retryPolicy,
            DeadLetterQueue dlq,
            TaskScheduler scheduler,
            ExecutionStore executionStore,
            String workerToken,
            UUID projectId) {

        this.engine = engine;

        this.registry = registry;

        this.retryPolicy = retryPolicy;

        this.dlq = dlq;

        this.scheduler = scheduler;

        this.executionStore = executionStore;

        this.projectId = projectId;

        this.dispatcher =
                new HttpDispatcher(workerToken);
    }


    public void execute() {

        engine.initialize();

        engine.scheduleReadyTasks(
                scheduler
        );

        String executionId =
                UUID.randomUUID().toString();

        System.out.println(
                "Execution ID: "
                        + executionId
        );


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


                for (UUID taskId :
                        taskIds) {

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
                                    node.getWorkerId(),
                                    projectId
                            );

                    executionStore.save(
                            execution
                    );


                    futures.add(
                            executor.submit(() -> {

                                TaskExecutionState state =
                                        executionStates
                                                .computeIfAbsent(
                                                        taskId,
                                                        key ->
                                                                new TaskExecutionState()
                                                );


                                while (true) {

                                    executionStore.markRunning(
                                            executionId,
                                            taskId
                                    );

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

                                        executionStore.markFailed(
                                                executionId,
                                                taskId,
                                                "Worker not found"
                                        );

                                        synchronized (engine) {

                                            engine.failTask(node);

                                            dlq.enqueue(
                                                    node,
                                                    state,
                                                    "Worker not found",
                                                    projectId
                                            );
                                        }

                                        scheduler.complete(
                                                taskId
                                        );

                                        break;
                                    }


                                    WorkerResponse response =
                                            dispatcher.dispatch(
                                                    worker
                                            );


                                    if (response.isSuccess()) {

                                        executionStore.markCompleted(
                                                executionId,
                                                taskId
                                        );


                                        synchronized (engine) {

                                            engine.completeTask(
                                                    node
                                            );
                                        }

                                        scheduler.complete(
                                                taskId
                                        );

                                        break;
                                    }


                                    state.setLastError(
                                            response.getMessage()
                                    );


                                    if (!response.shouldRetry()) {

                                        executionStore.markFailed(
                                                executionId,
                                                taskId,
                                                response.getMessage()
                                        );


                                        synchronized (engine) {

                                            engine.failTask(
                                                    node
                                            );

                                            dlq.enqueue(
                                                    node,
                                                    state,
                                                    response.getMessage(),
                                                    projectId
                                            );
                                        }


                                        scheduler.complete(
                                                taskId
                                        );

                                        break;
                                    }


                                    if (state.getAttemptCount()
                                            >= retryPolicy
                                            .getMaxRetries()) {

                                        executionStore.markFailed(
                                                executionId,
                                                taskId,
                                                response.getMessage()
                                        );


                                        synchronized (engine) {

                                            engine.failTask(
                                                    node
                                            );

                                            dlq.enqueue(
                                                    node,
                                                    state,
                                                    response.getMessage(),
                                                    projectId
                                            );
                                        }


                                        scheduler.complete(
                                                taskId
                                        );

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


                for (Future<?> future :
                        futures) {

                    future.get();
                }


            } catch (Exception e) {

                throw new RuntimeException(
                        e
                );
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


    public void reprocess(
            UUID taskId) {

        DeadLetterEntry entry =
                dlq.get(taskId);


        if (entry == null) {

            System.out.println(
                    "Task not found in DLQ: "
                            + taskId
            );

            return;
        }


        /*
         * Security check:
         *
         * The API key authenticated the CLI
         * as a particular project.
         *
         * A project must not be able to
         * reprocess another project's task.
         */
        if (!projectId.equals(
                entry.getProjectId()
        )) {

            System.out.println(
                    "Task does not belong "
                            + "to authenticated project."
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
                dispatcher.dispatch(
                        worker
                );


        if (response.isSuccess()) {

            synchronized (engine) {

                engine.completeTask(
                        node
                );
            }


            dlq.remove(
                    taskId
            );


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