package com.project.taskflow.queue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RedisTaskQueueIntegrationTest {

    private LettuceConnectionFactory factory;
    private StringRedisTemplate redis;
    private RedisTaskQueue queue;

    @BeforeEach
    void setUp() {
        factory = new LettuceConnectionFactory("localhost", 6379);
        factory.afterPropertiesSet();
        redis = new StringRedisTemplate(factory);
        queue = new RedisTaskQueue(redis);
        cleanRedis();
    }

    @AfterEach
    void tearDown() {
        cleanRedis();
        if (factory != null) {
            factory.destroy();
        }
    }

    private void cleanRedis() {
        try {
            redis.delete("taskflow:queue:tasks");
            redis.delete("taskflow:leases");
            var keys = redis.keys("taskflow:lease:*");
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    void testScheduleAndClaimTasks() {
        UUID task1 = UUID.randomUUID();
        UUID task2 = UUID.randomUUID();

        queue.add(task1, System.currentTimeMillis());
        queue.add(task2, System.currentTimeMillis());

        assertEquals(2, queue.size());

        // Claim 1 task
        List<UUID> claimed = queue.poll(1);
        assertEquals(1, claimed.size());
        UUID claimedTask = claimed.get(0);
        assertTrue(claimedTask.equals(task1) || claimedTask.equals(task2));

        // Queue size should now be 1
        assertEquals(1, queue.size());

        // Verify lease exists
        Boolean hasLease = redis.hasKey("taskflow:lease:" + claimedTask);
        assertTrue(Boolean.TRUE.equals(hasLease));

        // Complete the task
        queue.complete(claimedTask);

        // Verify lease removed
        hasLease = redis.hasKey("taskflow:lease:" + claimedTask);
        assertFalse(Boolean.TRUE.equals(hasLease));
    }

    @Test
    void testRecoverExpiredLeases() {
        UUID task = UUID.randomUUID();

        // Add and claim
        queue.add(task, System.currentTimeMillis());
        List<UUID> claimed = queue.poll(1);
        assertEquals(1, claimed.size());
        assertEquals(0, queue.size());

        // Artificially expire the lease in the LEASE_INDEX by updating its score to past
        redis.opsForZSet().add("taskflow:leases", task.toString(), System.currentTimeMillis() - 1000);

        // Recover expired leases
        queue.recoverExpired();

        // Task should be back in the ready queue
        assertEquals(1, queue.size());

        // And can be claimed again
        List<UUID> reclaimed = queue.poll(1);
        assertEquals(1, reclaimed.size());
        assertEquals(task, reclaimed.get(0));

        queue.complete(task);
    }
}
