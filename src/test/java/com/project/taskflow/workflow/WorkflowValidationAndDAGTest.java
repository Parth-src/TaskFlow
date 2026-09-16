package com.project.taskflow.workflow;

import com.project.taskflow.dependency.CycleDetector;
import com.project.taskflow.enums.TaskStatus;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.model.WorkflowNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class WorkflowValidationAndDAGTest {

    private final WorkflowParser parser = new WorkflowParser();
    private final WorkflowDefinitionBuilder builder = new WorkflowDefinitionBuilder();

    @Test
    void testValidWorkflowWithParameters() {
        String yaml = """
                name: payment-flow
                tasks:
                  - id: payment
                    worker: payment
                    params:
                      userId: 123
                      amount: 5000.50
                      currency: INR
                      sendReceipt: true
                      customer:
                        name: Parth
                      items:
                        - item1
                        - item2
                  - id: generate-invoice
                    worker: generate-invoice
                    depends_on:
                      - payment
                  - id: send-email
                    worker: send-email
                    depends_on:
                      - payment
                """;

        WorkflowDefinition def = parser.parse(yaml);
        assertNotNull(def);
        assertEquals("payment-flow", def.getName());
        assertEquals(3, def.getTasks().size());

        TaskDefinition paymentTask = def.getTasks().get(0);
        assertEquals("payment", paymentTask.getId());
        assertEquals("payment", paymentTask.getWorker());
        assertNotNull(paymentTask.getParams());
        assertEquals(123, paymentTask.getParams().get("userId"));
        assertEquals(5000.50, paymentTask.getParams().get("amount"));
        assertEquals("INR", paymentTask.getParams().get("currency"));
        assertEquals(true, paymentTask.getParams().get("sendReceipt"));

        Workflow workflow = builder.build(def);
        assertNotNull(workflow);
        assertEquals(3, workflow.getNodes().size());

        WorkflowNode paymentNode = workflow.getNodes().stream()
                .filter(n -> "payment".equals(n.getName()))
                .findFirst()
                .orElse(null);
        assertNotNull(paymentNode);
        assertEquals("payment", paymentNode.getWorkerId());
        assertEquals(123, paymentNode.getParams().get("userId"));
        assertEquals(2, paymentNode.getDependents().size());
        assertEquals(0, paymentNode.getDependencies().size());
    }

    @Test
    void testDuplicateTaskIdsRejected() {
        String yaml = """
                name: duplicate-test
                tasks:
                  - id: payment
                    worker: payment
                  - id: payment
                    worker: payment-retry
                """;

        WorkflowDefinition def = parser.parse(yaml);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(def)
        );
        assertTrue(ex.getMessage().contains("Duplicate task ID"));
    }

    @Test
    void testUnknownDependencyRejected() {
        String yaml = """
                name: unknown-dep-test
                tasks:
                  - id: send-email
                    worker: send-email
                    depends_on:
                      - non-existent-task
                """;

        WorkflowDefinition def = parser.parse(yaml);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(def)
        );
        assertTrue(ex.getMessage().contains("Unknown dependency"));
    }

    @Test
    void testCyclicDependencyRejected() {
        String yaml = """
                name: cycle-test
                tasks:
                  - id: task-a
                    worker: task-a
                    depends_on:
                      - task-b
                  - id: task-b
                    worker: task-b
                    depends_on:
                      - task-a
                """;

        WorkflowDefinition def = parser.parse(yaml);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(def)
        );
        assertTrue(ex.getMessage().contains("Cyclic dependency detected"));
    }

    @Test
    void testThreeNodeCycleRejected() {
        String yaml = """
                name: cycle-3-test
                tasks:
                  - id: A
                    worker: worker-a
                    depends_on:
                      - C
                  - id: B
                    worker: worker-b
                    depends_on:
                      - A
                  - id: C
                    worker: worker-c
                    depends_on:
                      - B
                """;

        WorkflowDefinition def = parser.parse(yaml);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> builder.build(def)
        );
        assertTrue(ex.getMessage().contains("Cyclic dependency detected"));
    }

    @Test
    void testFanOutFanInDAG() {
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

        WorkflowDefinition def = parser.parse(yaml);
        Workflow workflow = builder.build(def);
        assertNotNull(workflow);
        assertEquals(4, workflow.getNodes().size());
        assertFalse(CycleDetector.hasCycle(workflow));
    }
}
