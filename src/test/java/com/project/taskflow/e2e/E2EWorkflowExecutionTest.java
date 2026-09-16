package com.project.taskflow.e2e;

import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.dlq.RedisDeadLetterQueue;
import com.project.taskflow.execution.*;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.queue.RedisTaskQueue;
import com.project.taskflow.queue.RedisTaskScheduler;
import com.project.taskflow.queue.TaskScheduler;
import com.project.taskflow.retry.RetryPolicy;
import com.project.taskflow.worker.HttpDispatcher;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;
import com.project.taskflow.worker.WorkerResponse;
import com.project.taskflow.worker.server.JavaWorkerServer;
import com.project.taskflow.workflow.WorkflowDefinition;
import com.project.taskflow.workflow.WorkflowDefinitionBuilder;
import com.project.taskflow.workflow.WorkflowParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class E2EWorkflowExecutionTest {

    private static final int WORKER_PORT = 8089;
    private static final String WORKER_TOKEN = "e2e-test-token";
    private static JavaWorkerServer workerServer;
    private static LettuceConnectionFactory redisFactory;
    private static StringRedisTemplate redis;

    private final WorkflowParser parser = new WorkflowParser();
    private final WorkflowDefinitionBuilder builder = new WorkflowDefinitionBuilder();

    private WorkerRegistry registry;
    private DeadLetterQueue dlq;
    private TaskScheduler scheduler;
    private ExecutionStore executionStore;

    @BeforeAll
    static void init() throws IOException {
        workerServer = new JavaWorkerServer(WORKER_PORT, WORKER_TOKEN);
        workerServer.start();

        redisFactory = new LettuceConnectionFactory("localhost", 6379);
        redisFactory.afterPropertiesSet();
        redis = new StringRedisTemplate(redisFactory);
    }

    @AfterAll
    static void cleanup() {
        if (workerServer != null) {
            workerServer.stop();
        }
        if (redisFactory != null) {
            redisFactory.destroy();
        }
    }

    @BeforeEach
    void setUp() {
        workerServer.reset();
        cleanRedis();

        registry = new WorkerRegistry();
        String baseUrl = "http://127.0.0.1:" + WORKER_PORT;
        registry.discover(baseUrl);

        RedisTaskQueue queue = new RedisTaskQueue(redis);
        scheduler = new RedisTaskScheduler(queue);
        dlq = new RedisDeadLetterQueue(redis);
        executionStore = new RedisExecutionStore(redis);
    }

    private void cleanRedis() {
        try {
            redis.delete("taskflow:queue:tasks");
            redis.delete("taskflow:leases");
            redis.delete("taskflow:dlq");
            redis.delete("taskflow:executions");
            var keys = redis.keys("taskflow:*");
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    void testEndToEndSuccessfulWorkflowWithParameters() {
        String yaml = """
                name: e2e-payment-flow
                tasks:
                  - id: payment
                    worker: payment
                    params:
                      userId: 999
                      amount: 7500.25
                      currency: USD
                      sendReceipt: true
                      customer:
                        id: 999
                        name: "Parth"
                      items:
                        - "item-a"
                        - "item-b"
                  - id: generate-invoice
                    worker: generate-invoice
                    depends_on:
                      - payment
                  - id: send-email
                    worker: send-email
                    depends_on:
                      - payment
                  - id: analytics
                    worker: analytics
                    depends_on:
                      - payment
                """;

        WorkflowDefinition def = parser.parse(yaml);
        Workflow workflow = builder.build(def);
        ExecutionEngine engine = new ExecutionEngine(workflow);
        RetryPolicy retryPolicy = new RetryPolicy(3);
        UUID projectId = UUID.randomUUID();

        WorkflowExecutor executor = new WorkflowExecutor(
                engine,
                registry,
                retryPolicy,
                dlq,
                scheduler,
                executionStore,
                WORKER_TOKEN,
                projectId
        );

        String executionId = executor.execute();
        assertNotNull(executionId);

        // Verify task parameters reached worker server
        Map<String, Object> paymentParams = workerServer.getLastReceivedParams("payment");
        assertNotNull(paymentParams);
        assertEquals(999, paymentParams.get("userId"));
        assertEquals(7500.25, ((Number) paymentParams.get("amount")).doubleValue(), 0.001);
        assertEquals("USD", paymentParams.get("currency"));
        assertEquals(true, paymentParams.get("sendReceipt"));

        // Verify all tasks executed
        assertEquals(1, workerServer.getAttempts("payment"));
        assertEquals(1, workerServer.getAttempts("generate-invoice"));
        assertEquals(1, workerServer.getAttempts("send-email"));
        assertEquals(1, workerServer.getAttempts("analytics"));

        // Verify Redis execution store records
        List<TaskExecution> executions = executionStore.getByExecutionId(executionId);
        assertEquals(4, executions.size());
        for (TaskExecution exec : executions) {
            assertEquals("COMPLETED", exec.getStatus());
            assertNotNull(exec.getStartedAt());
            assertNotNull(exec.getCompletedAt());
            assertNull(exec.getError());
            assertEquals(1, exec.getAttempt());
        }
    }

    @Test
    void testEndToEndRetryExhaustionDLQAndReprocess() {
        String yaml = """
                name: e2e-retry-flow
                tasks:
                  - id: fail-task
                    worker: fail-worker
                """;

        WorkflowDefinition def = parser.parse(yaml);
        Workflow workflow = builder.build(def);
        ExecutionEngine engine = new ExecutionEngine(workflow);
        RetryPolicy retryPolicy = new RetryPolicy(3);
        UUID projectId = UUID.randomUUID();

        WorkflowExecutor executor = new WorkflowExecutor(
                engine,
                registry,
                retryPolicy,
                dlq,
                scheduler,
                executionStore,
                WORKER_TOKEN,
                projectId
        );

        // Force fail mode
        workerServer.setForceFail("fail-worker", true);

        String executionId = executor.execute();

        // Verify 3 retry attempts took place
        assertEquals(3, workerServer.getAttempts("fail-worker"));

        // Verify task entered DLQ
        List<DeadLetterEntry> dlqEntries = dlq.getEntries();
        assertEquals(1, dlqEntries.size());
        DeadLetterEntry entry = dlqEntries.get(0);
        assertEquals("fail-worker", entry.getWorkerId());
        assertEquals(3, entry.getAttemptCount());
        assertEquals(projectId, entry.getProjectId());

        // Verify execution store shows FAILED
        List<TaskExecution> executions = executionStore.getByExecutionId(executionId);
        assertEquals(1, executions.size());
        assertEquals("FAILED", executions.get(0).getStatus());

        // Now fix the worker to succeed and reprocess from DLQ
        workerServer.setForceFail("fail-worker", false);
        executor.reprocess(entry.getTaskId());

        // Verify DLQ is now empty
        assertTrue(dlq.getEntries().isEmpty());
    }

    @Test
    void testWorkerBearerAuthenticationRejection() {
        HttpDispatcher wrongTokenDispatcher = new HttpDispatcher("wrong-secret-token");
        WorkerMetadata worker = registry.get("payment");

        WorkerResponse response = wrongTokenDispatcher.dispatch(worker, Map.of("test", "data"));
        assertFalse(response.isSuccess());
        assertFalse(response.shouldRetry(), "Auth failure must NOT be retried");
        assertTrue(response.getMessage().contains("authentication failed") || response.getMessage().contains("401"));
    }

    @Test
    void testMultipleConcurrentExecutionsIsolation() {
        String yaml = """
                name: multi-exec
                tasks:
                  - id: payment
                    worker: payment
                    params:
                      currency: INR
                """;

        WorkflowDefinition def = parser.parse(yaml);
        RetryPolicy retryPolicy = new RetryPolicy(3);
        UUID projectId = UUID.randomUUID();

        Workflow workflow1 = builder.build(def);
        Workflow workflow2 = builder.build(def);

        ExecutionEngine engine1 = new ExecutionEngine(workflow1);
        ExecutionEngine engine2 = new ExecutionEngine(workflow2);

        WorkflowExecutor executor1 = new WorkflowExecutor(
                engine1, registry, retryPolicy, dlq, scheduler, executionStore, WORKER_TOKEN, projectId
        );
        WorkflowExecutor executor2 = new WorkflowExecutor(
                engine2, registry, retryPolicy, dlq, scheduler, executionStore, WORKER_TOKEN, projectId
        );

        String execId1 = executor1.execute();
        String execId2 = executor2.execute();

        assertNotEquals(execId1, execId2);

        List<TaskExecution> tasks1 = executionStore.getByExecutionId(execId1);
        List<TaskExecution> tasks2 = executionStore.getByExecutionId(execId2);

        assertEquals(1, tasks1.size());
        assertEquals(1, tasks2.size());
        assertEquals("COMPLETED", tasks1.get(0).getStatus());
        assertEquals("COMPLETED", tasks2.get(0).getStatus());
    }

    @Test
    void testParallelExecutionOfIndependentTasks() {
        String yaml = """
                name: parallel-flow
                tasks:
                  - id: start-task
                    worker: validate
                  - id: task-b
                    worker: send-email
                    depends_on:
                      - start-task
                    params:
                      sleepMs: 400
                  - id: task-c
                    worker: generate-invoice
                    depends_on:
                      - start-task
                    params:
                      sleepMs: 400
                  - id: task-d
                    worker: analytics
                    depends_on:
                      - start-task
                    params:
                      sleepMs: 400
                """;

        WorkflowDefinition def = parser.parse(yaml);
        Workflow workflow = builder.build(def);
        ExecutionEngine engine = new ExecutionEngine(workflow);
        RetryPolicy retryPolicy = new RetryPolicy(3);
        UUID projectId = UUID.randomUUID();

        WorkflowExecutor executor = new WorkflowExecutor(
                engine, registry, retryPolicy, dlq, scheduler, executionStore, WORKER_TOKEN, projectId
        );

        long start = System.currentTimeMillis();
        String executionId = executor.execute();
        long elapsed = System.currentTimeMillis() - start;

        // 3 parallel tasks of 400ms each: If sequential, it would be 3 * 400 = 1200ms+.
        // If parallel, it should finish in < 900ms.
        System.out.println("Parallel workflow completed in " + elapsed + " ms");
        assertTrue(elapsed < 1000, "Parallel tasks should execute concurrently in < 1000ms but took " + elapsed + "ms");

        List<TaskExecution> tasks = executionStore.getByExecutionId(executionId);
        assertEquals(4, tasks.size());
        for (TaskExecution task : tasks) {
            assertEquals("COMPLETED", task.getStatus());
        }
    }
}
