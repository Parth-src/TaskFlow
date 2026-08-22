package com.project.taskflow.execution;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RedisExecutionStore
        implements ExecutionStore {

    private final StringRedisTemplate redis;

    public RedisExecutionStore(
            StringRedisTemplate redis) {

        this.redis = redis;
    }

    private String key(UUID taskId) {

        return "taskflow:task:" + taskId;
    }

    @Override
    public void save(TaskExecution execution) {

        redis.opsForHash().putAll(
                key(execution.getTaskId()),
                Map.of(
                        "taskId",
                        execution.getTaskId().toString(),

                        "workflowId",
                        execution.getWorkflowId(),

                        "executionId",
                        execution.getExecutionId(),

                        "workerId",
                        execution.getWorkerId(),

                        "status",
                        execution.getStatus(),

                        "attempt",
                        String.valueOf(
                                execution.getAttempt()
                        ),

                        "createdAt",
                        execution.getCreatedAt().toString()
                )

        );

        redis.opsForSet().add(
                "taskflow:execution:"
                        + execution.getExecutionId()
                        + ":tasks",
                execution.getTaskId().toString()
        );
    }

    @Override
    public TaskExecution get(UUID taskId) {

        Map<Object, Object> data =
                redis.opsForHash().entries(
                        key(taskId)
                );

        if (data.isEmpty()) {
            return null;
        }

        return new TaskExecution(
                UUID.fromString(
                        (String) data.get("taskId")
                ),

                (String) data.get("workflowId"),

                (String) data.get("executionId"),

                (String) data.get("workerId"),

                (String) data.get("status"),

                Integer.parseInt(
                        (String) data.get("attempt")
                ),

                Instant.parse(
                        (String) data.get("createdAt")
                ),

                data.get("startedAt") == null
                        ? null
                        : Instant.parse(
                        (String) data.get("startedAt")
                ),

                data.get("completedAt") == null
                        ? null
                        : Instant.parse(
                        (String) data.get("completedAt")
                ),

                data.get("error") == null
                        ? null
                        : (String) data.get("error")
        );
    }

    @Override
    public void markRunning(UUID taskId) {

        redis.opsForHash().put(
                key(taskId),
                "status",
                "RUNNING"
        );

        redis.opsForHash().put(
                key(taskId),
                "startedAt",
                Instant.now().toString()
        );

        redis.opsForHash().increment(
                key(taskId),
                "attempt",
                1
        );
    }

    @Override
    public void markCompleted(
            UUID taskId) {

        redis.opsForHash().put(
                key(taskId),
                "status",
                "COMPLETED"
        );

        redis.opsForHash().put(
                key(taskId),
                "completedAt",
                Instant.now().toString()
        );
    }

    @Override
    public void markFailed(
            UUID taskId,
            String reason) {

        redis.opsForHash().put(
                key(taskId),
                "status",
                "FAILED"
        );

        redis.opsForHash().put(
                key(taskId),
                "error",
                reason
        );

        redis.opsForHash().put(
                key(taskId),
                "completedAt",
                Instant.now().toString()
        );
    }

    public List<TaskExecution> getByExecutionId(
            String executionId) {

        String indexKey =
                "taskflow:execution:"
                        + executionId
                        + ":tasks";

        var taskIds =
                redis.opsForSet().members(
                        indexKey
                );

        if (taskIds == null ||
                taskIds.isEmpty()) {

            return List.of();
        }

        List<TaskExecution> executions =
                new ArrayList<>();

        for (String taskId : taskIds) {

            TaskExecution execution =
                    get(
                            UUID.fromString(taskId)
                    );

            if (execution != null) {

                executions.add(execution);
            }
        }

        return executions;
    }
}