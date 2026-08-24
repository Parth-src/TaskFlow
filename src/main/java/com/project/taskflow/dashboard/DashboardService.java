package com.project.taskflow.dashboard;

import com.project.taskflow.dashboard.dto.TaskExecutionDTO;
import com.project.taskflow.execution.ExecutionStore;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

import java.util.ArrayList;
import java.util.List;

public class DashboardService {

    private final WorkerRegistry workerRegistry;

    private final ExecutionStore executionStore;


    public DashboardService(
            WorkerRegistry workerRegistry,
            ExecutionStore executionStore) {

        this.workerRegistry =
                workerRegistry;

        this.executionStore =
                executionStore;
    }


    public List<WorkerMetadata> getWorkers() {

        return new ArrayList<>(
                workerRegistry.getAll()
        );
    }


    public List<TaskExecutionDTO> getExecution(
            String executionId) {

        return executionStore
                .getByExecutionId(
                        executionId
                )
                .stream()
                .map(TaskExecutionDTO::new)
                .toList();
    }

}