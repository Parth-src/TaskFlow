package com.project.taskflow.execution;

import com.project.taskflow.enums.TaskStatus;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;
import com.project.taskflow.queue.TaskScheduler;
import com.project.taskflow.workflow.WorkflowDefinition;
import com.project.taskflow.workflow.WorkflowDefinitionBuilder;
import com.project.taskflow.workflow.WorkflowParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ExecutionEngineTest {

    private final WorkflowParser parser = new WorkflowParser();
    private final WorkflowDefinitionBuilder builder = new WorkflowDefinitionBuilder();

    static class MockScheduler implements TaskScheduler {
        final List<UUID> scheduled = new ArrayList<>();

        @Override
        public void schedule(UUID taskId, long availableAt) {
            scheduled.add(taskId);
        }

        @Override
        public List<UUID> nextTasks(int limit) {
            return List.copyOf(scheduled);
        }

        @Override
        public void complete(UUID taskId) {
            scheduled.remove(taskId);
        }

        @Override
        public void recoverExpired() {
        }
    }

    @Test
    void testSingleTaskExecution() {
        String yaml = """
                name: single
                tasks:
                  - id: A
                    worker: worker-a
                """;

        Workflow workflow = builder.build(parser.parse(yaml));
        ExecutionEngine engine = new ExecutionEngine(workflow);

        engine.initialize();
        List<WorkflowNode> ready = engine.getReadyTasks();
        assertEquals(1, ready.size());
        assertEquals("A", ready.get(0).getName());

        MockScheduler scheduler = new MockScheduler();
        engine.scheduleReadyTasks(scheduler);
        assertEquals(1, scheduler.scheduled.size());

        engine.completeTask(ready.get(0));
        assertEquals(TaskStatus.COMPLETED, ready.get(0).getStatus());
        assertEquals(0, engine.getReadyTasks().size());
    }

    @Test
    void testSequentialDependencyResolution() {
        String yaml = """
                name: sequential
                tasks:
                  - id: A
                    worker: worker-a
                  - id: B
                    worker: worker-b
                    depends_on:
                      - A
                  - id: C
                    worker: worker-c
                    depends_on:
                      - B
                """;

        Workflow workflow = builder.build(parser.parse(yaml));
        ExecutionEngine engine = new ExecutionEngine(workflow);

        engine.initialize();
        List<WorkflowNode> ready = engine.getReadyTasks();
        assertEquals(1, ready.size());
        assertEquals("A", ready.get(0).getName());

        WorkflowNode nodeA = ready.get(0);
        WorkflowNode nodeB = workflow.getNodes().stream().filter(n -> "B".equals(n.getName())).findFirst().orElseThrow();
        WorkflowNode nodeC = workflow.getNodes().stream().filter(n -> "C".equals(n.getName())).findFirst().orElseThrow();

        assertNull(nodeB.getStatus());
        assertNull(nodeC.getStatus());

        // Complete A -> B becomes READY
        engine.completeTask(nodeA);
        assertEquals(TaskStatus.COMPLETED, nodeA.getStatus());
        assertEquals(TaskStatus.READY, nodeB.getStatus());
        assertNull(nodeC.getStatus());

        // Complete B -> C becomes READY
        engine.completeTask(nodeB);
        assertEquals(TaskStatus.COMPLETED, nodeB.getStatus());
        assertEquals(TaskStatus.READY, nodeC.getStatus());

        // Complete C -> All complete
        engine.completeTask(nodeC);
        assertEquals(TaskStatus.COMPLETED, nodeC.getStatus());
        assertTrue(engine.getReadyTasks().isEmpty());
    }

    @Test
    void testFanOutFanInDependencyResolution() {
        String yaml = """
                name: fanout-fanin
                tasks:
                  - id: A
                    worker: worker-a
                  - id: B
                    worker: worker-b
                    depends_on:
                      - A
                  - id: C
                    worker: worker-c
                    depends_on:
                      - A
                  - id: D
                    worker: worker-d
                    depends_on:
                      - B
                      - C
                """;

        Workflow workflow = builder.build(parser.parse(yaml));
        ExecutionEngine engine = new ExecutionEngine(workflow);

        engine.initialize();
        List<WorkflowNode> ready = engine.getReadyTasks();
        assertEquals(1, ready.size());
        assertEquals("A", ready.get(0).getName());

        WorkflowNode nodeA = ready.get(0);
        WorkflowNode nodeB = workflow.getNodes().stream().filter(n -> "B".equals(n.getName())).findFirst().orElseThrow();
        WorkflowNode nodeC = workflow.getNodes().stream().filter(n -> "C".equals(n.getName())).findFirst().orElseThrow();
        WorkflowNode nodeD = workflow.getNodes().stream().filter(n -> "D".equals(n.getName())).findFirst().orElseThrow();

        // Complete A -> Both B and C become READY (Fan-out)
        engine.completeTask(nodeA);
        assertEquals(TaskStatus.READY, nodeB.getStatus());
        assertEquals(TaskStatus.READY, nodeC.getStatus());
        assertNull(nodeD.getStatus());

        // Complete B only -> D is NOT ready yet because C is not completed
        engine.completeTask(nodeB);
        assertEquals(TaskStatus.COMPLETED, nodeB.getStatus());
        assertNull(nodeD.getStatus());

        // Complete C -> D now becomes READY (Fan-in)
        engine.completeTask(nodeC);
        assertEquals(TaskStatus.COMPLETED, nodeC.getStatus());
        assertEquals(TaskStatus.READY, nodeD.getStatus());

        // Complete D
        engine.completeTask(nodeD);
        assertEquals(TaskStatus.COMPLETED, nodeD.getStatus());
    }

    @Test
    void testTaskFailureState() {
        String yaml = """
                name: fail-test
                tasks:
                  - id: A
                    worker: worker-a
                """;

        Workflow workflow = builder.build(parser.parse(yaml));
        ExecutionEngine engine = new ExecutionEngine(workflow);
        engine.initialize();

        WorkflowNode nodeA = engine.getReadyTasks().get(0);
        engine.failTask(nodeA);
        assertEquals(TaskStatus.FAILED, nodeA.getStatus());
    }
}
