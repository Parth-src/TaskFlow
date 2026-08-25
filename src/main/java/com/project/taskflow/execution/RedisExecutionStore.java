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
    private static final String EXECUTION_INDEX =
            "taskflow:executions";

    public RedisExecutionStore(
            StringRedisTemplate redis) {

        this.redis = redis;
    }

    private String key(
            String executionId,
            UUID taskId) {

        return "taskflow:execution:"
                + executionId
                + ":task:"
                + taskId;
    }

    private String indexKey(
            String executionId) {

        return "taskflow:execution:"
                + executionId
                + ":tasks";
    }

    @Override
    public void save(
            TaskExecution execution) {

        String executionId =
                execution.getExecutionId();

        UUID taskId =
                execution.getTaskId();

        redis.opsForHash().putAll(
                key(
                        executionId,
                        taskId
                ),
                Map.of(
                        "taskId",
                        taskId.toString(),

                        "workflowId",
                        execution.getWorkflowId(),

                        "executionId",
                        executionId,

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
                indexKey(executionId),
                taskId.toString()
        );

        redis.opsForZSet().add(
                EXECUTION_INDEX,
                execution.getExecutionId(),
                execution.getCreatedAt().toEpochMilli()
        );
    }

    @Override
    public TaskExecution get(
            String executionId,
            UUID taskId) {

        String redisKey =
                key(
                        executionId,
                        taskId
                );

        System.out.println(
                "Looking up Redis key: "
                        + redisKey
        );

        Map<Object, Object> data =
                redis.opsForHash().entries(
                        redisKey
                );

        System.out.println(
                "Redis data: "
                        + data
        );

        if (data.isEmpty()) {

            System.out.println(
                    "NO EXECUTION RECORD FOUND"
            );

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
    public void markRunning(
            String executionId,
            UUID taskId) {

        String key =
                key(
                        executionId,
                        taskId
                );

        redis.opsForHash().put(
                key,
                "status",
                "RUNNING"
        );

        redis.opsForHash().put(
                key,
                "startedAt",
                Instant.now().toString()
        );

        redis.opsForHash().increment(
                key,
                "attempt",
                1
        );

        redis.opsForHash().delete(
                key,
                "error"
        );
    }

    @Override
    public void markCompleted(
            String executionId,
            UUID taskId) {

        String key =
                key(
                        executionId,
                        taskId
                );

        redis.opsForHash().put(
                key,
                "status",
                "COMPLETED"
        );

        redis.opsForHash().put(
                key,
                "completedAt",
                Instant.now().toString()
        );

        redis.opsForHash().delete(
                key,
                "error"
        );
    }

    @Override
    public void markFailed(
            String executionId,
            UUID taskId,
            String reason) {

        String key =
                key(
                        executionId,
                        taskId
                );

        redis.opsForHash().put(
                key,
                "status",
                "FAILED"
        );

        redis.opsForHash().put(
                key,
                "error",
                reason
        );

        redis.opsForHash().put(
                key,
                "completedAt",
                Instant.now().toString()
        );
    }

    @Override
    public List<TaskExecution> getByExecutionId(
            String executionId) {

        var taskIds =
                redis.opsForSet().members(
                        indexKey(executionId)
                );

        if (taskIds == null ||
                taskIds.isEmpty()) {

            return List.of();
        }

        List<TaskExecution> executions =
                new ArrayList<>();

        for (String taskId :
                taskIds) {

            TaskExecution execution =
                    get(
                            executionId,
                            UUID.fromString(taskId)
                    );

            if (execution != null) {

                executions.add(execution);
            }
        }

        return executions;
    }

    @Override
    public List<TaskExecution> getRecentExecutions(
            int limit) {

        if (limit <= 0) {
            return List.of();
        }

        var executionIds =
                redis.opsForZSet()
                        .reverseRange(
                                EXECUTION_INDEX,
                                0,
                                limit - 1
                        );

        if (executionIds == null ||
                executionIds.isEmpty()) {

            return List.of();
        }

        List<TaskExecution> executions =
                new ArrayList<>();

        for (String executionId :
                executionIds) {

            List<TaskExecution> tasks =
                    getByExecutionId(
                            executionId
                    );

            /*
             * One execution contains multiple tasks.
             * We only need one task here to represent
             * the execution in the execution list.
             */
            if (!tasks.isEmpty()) {

                executions.add(
                        tasks.get(0)
                );
            }
        }

        return executions;
    }
}