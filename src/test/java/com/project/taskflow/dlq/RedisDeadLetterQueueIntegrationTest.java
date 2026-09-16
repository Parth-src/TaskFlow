package com.project.taskflow.dlq;

import com.project.taskflow.execution.TaskExecutionState;
import com.project.taskflow.model.WorkflowNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class RedisDeadLetterQueueIntegrationTest {

    private LettuceConnectionFactory factory;
    private StringRedisTemplate redis;
    private RedisDeadLetterQueue dlq;

    @BeforeEach
    void setUp() {
        factory = new LettuceConnectionFactory("localhost", 6379);
        factory.afterPropertiesSet();
        redis = new StringRedisTemplate(factory);
        dlq = new RedisDeadLetterQueue(redis);
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
            redis.delete("taskflow:dlq");
            var keys = redis.keys("taskflow:dlq:*");
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    void testEnqueueAndRetrieveFromDLQ() {
        UUID taskId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        WorkflowNode node = new WorkflowNode(taskId, "payment");
        TaskExecutionState state = new TaskExecutionState();
        state.incrementAttempt();
        state.incrementAttempt();
        state.incrementAttempt();

        dlq.enqueue(node, state, "Payment gateway timed out", projectId);

        List<DeadLetterEntry> entries = dlq.getEntries();
        assertFalse(entries.isEmpty());

        DeadLetterEntry entry = dlq.get(taskId);
        assertNotNull(entry);
        assertEquals(taskId, entry.getTaskId());
        assertEquals("payment", entry.getWorkerId());
        assertEquals(3, entry.getAttemptCount());
        assertEquals("Payment gateway timed out", entry.getReason());
        assertEquals(projectId, entry.getProjectId());

        // Remove from DLQ
        dlq.remove(taskId);
        assertNull(dlq.get(taskId));
        assertTrue(dlq.getEntries().stream().noneMatch(e -> e.getTaskId().equals(taskId)));
    }
}
