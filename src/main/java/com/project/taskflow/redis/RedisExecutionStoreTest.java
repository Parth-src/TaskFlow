package com.project.taskflow.redis;

import com.project.taskflow.execution.RedisExecutionStore;
import com.project.taskflow.execution.TaskExecution;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

public class RedisExecutionStoreTest {

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

            UUID taskId =
                    UUID.randomUUID();

            String executionId =
                    UUID.randomUUID().toString();

            TaskExecution execution =
                    new TaskExecution(
                            taskId,
                            "payment-flow",
                            executionId,
                            "payment"
                    );

            System.out.println(
                    "Task ID: " + taskId
            );

            System.out.println(
                    "Execution ID: " + executionId
            );

            store.save(execution);

            System.out.println(
                    "Task saved."
            );

            store.markRunning(
                    executionId,
                    taskId
            );

            System.out.println(
                    "Task marked RUNNING."
            );

            store.markCompleted(
                    executionId,
                    taskId
            );

            System.out.println(
                    "Task marked COMPLETED."
            );

            System.out.println(
                    "Redis data:"
            );

            redis.opsForHash()
                    .entries(
                            "taskflow:execution:"
                                    + executionId
                                    + ":task:"
                                    + taskId
                    )
                    .forEach(
                            (key, value) ->
                                    System.out.println(
                                            key + " = " + value
                                    )
                    );

        } finally {

            factory.destroy();
        }
    }
}