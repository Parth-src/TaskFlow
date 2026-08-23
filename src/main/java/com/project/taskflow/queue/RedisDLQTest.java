package com.project.taskflow.redis;

import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.model.WorkflowNode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

public class RedisDLQTest {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                SpringApplication.run(
                        com.project.taskflow.TaskflowApplication.class,
                        args
                );

        DeadLetterQueue dlq =
                context.getBean(
                        DeadLetterQueue.class
                );

        WorkflowNode node =
                new WorkflowNode(
                        "test-worker"
                );

        /*
         * Simulate a task that exhausted retries.
         */
        com.project.taskflow.execution.TaskExecutionState state =
                new com.project.taskflow.execution.TaskExecutionState();

        state.incrementAttempt();
        state.incrementAttempt();
        state.incrementAttempt();

        dlq.enqueue(
                node,
                state,
                "Test failure"
        );

        System.out.println(
                "DLQ entries: "
                        + dlq.getEntries().size()
        );

        List<DeadLetterEntry> entries =
                dlq.getEntries();

        for (DeadLetterEntry entry :
                entries) {

            System.out.println(
                    "Task ID: "
                            + entry.getTaskId()
            );

            System.out.println(
                    "Worker: "
                            + entry.getWorkerId()
            );

            System.out.println(
                    "Attempts: "
                            + entry.getAttemptCount()
            );

            System.out.println(
                    "Reason: "
                            + entry.getReason()
            );
        }

        context.close();
    }
}