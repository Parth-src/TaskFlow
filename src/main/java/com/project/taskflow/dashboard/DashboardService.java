package com.project.taskflow.dashboard;

import com.project.taskflow.dashboard.dto.ExecutionSummaryDTO;
import com.project.taskflow.dashboard.dto.TaskExecutionDTO;
import com.project.taskflow.execution.ExecutionStore;
import com.project.taskflow.execution.TaskExecution;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
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
            UUID projectId,
            String executionId) {

        List<TaskExecution> executions =
                executionStore
                        .getByExecutionId(
                                executionId
                        );

        if (executions.isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Execution not found"
            );
        }

        /*
         * Every task belonging to this execution
         * must belong to the authenticated project.
         */
        for (TaskExecution execution :
                executions) {

            if (!projectId.equals(
                    execution.getProjectId()
            )) {

                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have access to this execution"
                );
            }
        }

        return executions
                .stream()
                .map(TaskExecutionDTO::new)
                .toList();
    }

    public List<ExecutionSummaryDTO> getRecentExecutions(
            UUID projectId,
            int limit) {

        if (limit <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Limit must be greater than zero"
            );
        }

        if (limit > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Limit cannot exceed 100"
            );
        }

        return executionStore
                .getRecentExecutions(
                        projectId,
                        limit
                )
                .stream()
                .map(ExecutionSummaryDTO::new)
                .toList();
    }
}