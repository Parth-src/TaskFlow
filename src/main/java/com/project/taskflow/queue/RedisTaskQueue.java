package com.project.taskflow.queue;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.*;

public class RedisTaskQueue implements TaskQueue {

    private static final String QUEUE_KEY =
            "queue:tasks";

    private final StringRedisTemplate redis;

    private final DefaultRedisScript<List> claimScript;

    public RedisTaskQueue(
            StringRedisTemplate redis) {

        this.redis = redis;

        this.claimScript =
                new DefaultRedisScript<>();

        this.claimScript.setScriptText(
                """
                local tasks = redis.call(
                    'ZRANGEBYSCORE',
                    KEYS[1],
                    '-inf',
                    ARGV[1],
                    'LIMIT',
                    0,
                    ARGV[2]
                )

                for _, taskId in ipairs(tasks) do

                    redis.call(
                        'ZREM',
                        KEYS[1],
                        taskId
                    )

                end

                return tasks
                """
        );

        this.claimScript.setResultType(
                List.class
        );
    }

    @Override
    public void add(
            UUID taskId,
            long availableAt) {

        redis.opsForZSet().add(
                QUEUE_KEY,
                taskId.toString(),
                availableAt
        );
    }

    @Override
    public List<UUID> poll(
            int maxTasks) {

        if (maxTasks <= 0) {
            return List.of();
        }

        List<String> taskIds =
                redis.execute(
                        claimScript,
                        Collections.singletonList(
                                QUEUE_KEY
                        ),
                        String.valueOf(
                                System.currentTimeMillis()
                        ),
                        String.valueOf(maxTasks)
                );

        if (taskIds == null ||
                taskIds.isEmpty()) {

            return List.of();
        }

        List<UUID> result =
                new ArrayList<>();

        for (String taskId : taskIds) {

            result.add(
                    UUID.fromString(taskId)
            );
        }

        return result;
    }

    @Override
    public long size() {

        Long size =
                redis.opsForZSet().zCard(
                        QUEUE_KEY
                );

        return size == null
                ? 0
                : size;
    }
}