package com.project.taskflow.redis;

import com.project.taskflow.execution.RedisExecutionStore;
import com.project.taskflow.execution.TaskExecution;
import com.project.taskflow.monitoring.RedisExecutionMonitor;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

public class RedisExecutionMonitorTest {

    public static void main(String[] args) {

        LettuceConnectionFactory factory =
                new LettuceConnectionFactory(
                        "localhost",
                        6379
                );

        factory.afterPropertiesSet();

        try {

            StringRedisTemplate redis =
                    new StringRedisTemplate(factory);

            RedisExecutionStore store =
                    new RedisExecutionStore(redis);

            RedisExecutionMonitor monitor =
                    new RedisExecutionMonitor(store);

            /*
             * Find an existing execution.
             *
             * Replace this with the execution ID
             * from your Redis output.
             */
            String executionId =
                    "a94d99ee-a773-461f-a0b8-a1e302b10b34";

            List<TaskExecution> tasks =
                    monitor.getWorkflowTasks(
                            executionId
                    );

            System.out.println(
                    "Execution: "
                            + executionId
            );

            System.out.println(
                    "Tasks: "
                            + tasks.size()
            );

            for (TaskExecution task : tasks) {

                System.out.println(
                        "Task: "
                                + task.getTaskId()
                );

                System.out.println(
                        "Worker: "
                                + task.getWorkerId()
                );

                System.out.println(
                        "Status: "
                                + task.getStatus()
                );

                System.out.println(
                        "Attempt: "
                                + task.getAttempt()
                );

                System.out.println(
                        "-------------------------"
                );
            }

        } finally {

            factory.destroy();
        }
    }
}