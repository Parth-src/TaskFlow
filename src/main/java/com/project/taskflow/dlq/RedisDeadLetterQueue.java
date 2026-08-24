package com.project.taskflow.dlq;

import com.project.taskflow.execution.TaskExecutionState;
import com.project.taskflow.model.WorkflowNode;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RedisDeadLetterQueue
        implements DeadLetterQueue {

    private static final String INDEX =
            "taskflow:dlq";

    private final StringRedisTemplate redis;


    public RedisDeadLetterQueue(
            StringRedisTemplate redis) {

        this.redis = redis;
    }


    private String key(UUID taskId) {

        return "taskflow:dlq:" + taskId;
    }


    @Override
    public void enqueue(
            WorkflowNode node,
            TaskExecutionState state,
            String reason) {

        UUID taskId =
                node.getId();

        Instant timestamp =
                Instant.now();

        Map<String, String> data =
                new HashMap<>();

        data.put(
                "taskId",
                taskId.toString()
        );

        data.put(
                "workerId",
                node.getWorkerId()
        );

        data.put(
                "attemptCount",
                String.valueOf(
                        state.getAttemptCount()
                )
        );

        data.put(
                "reason",
                reason
        );

        data.put(
                "timestamp",
                timestamp.toString()
        );


        redis.opsForHash().putAll(
                key(taskId),
                data
        );


        redis.opsForSet().add(
                INDEX,
                taskId.toString()
        );


        System.out.println(
                "Task moved to Redis DLQ: "
                        + taskId
        );
    }


    @Override
    public List<DeadLetterEntry> getEntries() {

        Set<String> taskIds =
                redis.opsForSet().members(
                        INDEX
                );

        if (taskIds == null ||
                taskIds.isEmpty()) {

            return List.of();
        }


        List<DeadLetterEntry> entries =
                new ArrayList<>();


        for (String taskId :
                taskIds) {

            DeadLetterEntry entry =
                    get(
                            UUID.fromString(
                                    taskId
                            )
                    );

            if (entry != null) {

                entries.add(entry);
            }
        }


        return entries;
    }


    @Override
    public DeadLetterEntry get(
            UUID taskId) {

        Map<Object, Object> data =
                redis.opsForHash().entries(
                        key(taskId)
                );

        if (data == null ||
                data.isEmpty()) {

            return null;
        }


        String workerId =
                (String) data.get(
                        "workerId"
                );


        int attemptCount =
                Integer.parseInt(
                        (String) data.get(
                                "attemptCount"
                        )
                );


        String reason =
                (String) data.get(
                        "reason"
                );


        String timestamp =
                (String) data.get(
                        "timestamp"
                );


        /*
         * For now we reconstruct a minimal
         * WorkflowNode containing the fields
         * required by the DLQ.
         */
        WorkflowNode node =
                new WorkflowNode(
                        taskId,
                        workerId
                );


        DeadLetterEntry entry =
                new DeadLetterEntry(
                        node,
                        attemptCount,
                        reason,
                        Instant.parse(
                                timestamp
                        )
                );


        return entry;
    }


    @Override
    public void remove(
            UUID taskId) {

        redis.delete(
                key(taskId)
        );

        redis.opsForSet().remove(
                INDEX,
                taskId.toString()
        );
    }
}