package com.project.taskflow.execution;

import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.worker.HttpDispatcher;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.List;

public class WorkflowExecutor {

    private final ExecutionEngine engine;

    private final WorkerRegistry registry;

    private final HttpDispatcher dispatcher;

    public WorkflowExecutor(
            ExecutionEngine engine,
            WorkerRegistry registry) {

        this.engine = engine;

        this.registry = registry;

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

                                boolean success =
                                        dispatcher.dispatch(
                                                worker
                                        );

                                if (success) {

                                    synchronized (engine) {

                                        engine.completeTask(
                                                node
                                        );
                                    }
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

}