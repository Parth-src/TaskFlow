package com.project.taskflow.execution;

import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.worker.HttpDispatcher;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

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

            for (WorkflowNode node : readyTasks) {

                WorkerMetadata worker =
                        registry.get(
                                node.getWorkerId()
                        );

                if (worker == null) {

                    System.out.println(
                            "Worker not found: "
                                    + node.getWorkerId()
                    );

                    continue;
                }

                boolean success =
                        dispatcher.dispatch(
                                worker
                        );

                if (success) {

                    engine.completeTask(
                            node
                    );
                }
            }
        }

        System.out.println(
                "Workflow completed."
        );
    }
}