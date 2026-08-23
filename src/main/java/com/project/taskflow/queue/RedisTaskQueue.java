package com.project.taskflow.queue;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RedisTaskQueue
        implements TaskQueue {

    private static final String QUEUE_KEY =
            "taskflow:queue:tasks";

    private static final String LEASE_PREFIX =
            "taskflow:lease:";

    private static final String LEASE_INDEX =
            "taskflow:leases";

    /*
     * Task lease duration.
     *
     * If a worker/process crashes and does not
     * complete the task, the lease expires after
     * this amount of time.
     */
    private static final long LEASE_MILLIS =
            30_000;

    private final StringRedisTemplate redis;

    private final DefaultRedisScript<List>
            claimScript;

    private final DefaultRedisScript<List>
            recoveryScript;

    public RedisTaskQueue(
            StringRedisTemplate redis) {

        this.redis = redis;


        // =====================================================
        // CLAIM SCRIPT
        // =====================================================

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

                    redis.call(
                        'SET',
                        KEYS[2] .. taskId,
                        ARGV[3],
                        'PX',
                        ARGV[4]
                    )

                    redis.call(
                        'ZADD',
                        KEYS[3],
                        ARGV[5],
                        taskId
                    )

                end

                return tasks
                """
        );

        this.claimScript.setResultType(
                List.class
        );


        // =====================================================
        // RECOVERY SCRIPT
        // =====================================================

        this.recoveryScript =
                new DefaultRedisScript<>();

        this.recoveryScript.setScriptText(
                """
                local expired = redis.call(
                    'ZRANGEBYSCORE',
                    KEYS[1],
                    '-inf',
                    ARGV[1]
                )

                for _, taskId in ipairs(expired) do

                    redis.call(
                        'ZREM',
                        KEYS[1],
                        taskId
                    )

                    redis.call(
                        'DEL',
                        KEYS[2] .. taskId
                    )

                    redis.call(
                        'ZADD',
                        KEYS[3],
                        ARGV[1],
                        taskId
                    )

                end

                return expired
                """
        );

        this.recoveryScript.setResultType(
                List.class
        );
    }


    // =========================================================
    // ADD
    // =========================================================

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


    // =========================================================
    // POLL / CLAIM
    // =========================================================

    @Override
    public List<UUID> poll(
            int maxTasks) {

        if (maxTasks <= 0) {

            return List.of();
        }

        String ownerId =
                UUID.randomUUID().toString();

        long now =
                System.currentTimeMillis();

        long leaseUntil =
                now + LEASE_MILLIS;


        List<String> taskIds =
                redis.execute(

                        claimScript,

                        Arrays.asList(
                                QUEUE_KEY,
                                LEASE_PREFIX,
                                LEASE_INDEX
                        ),

                        String.valueOf(
                                now
                        ),

                        String.valueOf(
                                maxTasks
                        ),

                        ownerId,

                        String.valueOf(
                                LEASE_MILLIS
                        ),

                        String.valueOf(
                                leaseUntil
                        )
                );


        if (taskIds == null ||
                taskIds.isEmpty()) {

            return List.of();
        }


        List<UUID> result =
                new ArrayList<>();

        for (String taskId :
                taskIds) {

            result.add(
                    UUID.fromString(taskId)
            );
        }

        return result;
    }


    // =========================================================
    // SIZE
    // =========================================================

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


    // =========================================================
    // COMPLETE
    // =========================================================

    @Override
    public void complete(
            UUID taskId) {

        redis.delete(
                LEASE_PREFIX
                        + taskId
        );

        redis.opsForZSet().remove(
                LEASE_INDEX,
                taskId.toString()
        );
    }


    // =========================================================
    // RECOVER EXPIRED TASKS
    // =========================================================

    @Override
    public void recoverExpired() {

        long now =
                System.currentTimeMillis();


        List<String> expired =
                redis.execute(

                        recoveryScript,

                        Arrays.asList(
                                LEASE_INDEX,
                                LEASE_PREFIX,
                                QUEUE_KEY
                        ),

                        String.valueOf(
                                now
                        )
                );


        if (expired == null ||
                expired.isEmpty()) {

            return;
        }


        System.out.println(
                "Recovered "
                        + expired.size()
                        + " expired task(s)."
        );
    }
}