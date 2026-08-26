package com.project.taskflow.queue;

import com.project.taskflow.TaskflowApplication;
import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.execution.TaskExecutionState;
import com.project.taskflow.model.WorkflowNode;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.UUID;

public class RedisDLQTest {

    public static void main(String[] args) {

        ConfigurableApplicationContext context =
                SpringApplication.run(
                        TaskflowApplication.class,
                        args
                );

        try {

            DeadLetterQueue dlq =
                    context.getBean(
                            DeadLetterQueue.class
                    );

            WorkflowNode node =
                    new WorkflowNode(
                            "test-worker"
                    );

            /*
             * Simulate a project.
             *
             * In the real CLI this comes from
             * the authenticated API key.
             */
            UUID projectId =
                    UUID.randomUUID();

            /*
             * Simulate a task that exhausted retries.
             */
            TaskExecutionState state =
                    new TaskExecutionState();

            state.incrementAttempt();
            state.incrementAttempt();
            state.incrementAttempt();

            dlq.enqueue(
                    node,
                    state,
                    "Test failure",
                    projectId
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
                        "Project ID: "
                                + entry.getProjectId()
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

                System.out.println(
                        "Timestamp: "
                                + entry.getTimestamp()
                );
            }

        } finally {

            context.close();
        }
    }
}