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

            TaskExecution execution =
                    new TaskExecution(
                            taskId,
                            "payment-flow",
                            UUID.randomUUID().toString(),
                            "payment"
                    );

            System.out.println(
                    "Task ID: " + taskId
            );

            store.save(execution);

            System.out.println(
                    "Task saved."
            );

            store.markRunning(taskId);

            System.out.println(
                    "Task marked RUNNING."
            );

            store.markCompleted(taskId);

            System.out.println(
                    "Task marked COMPLETED."
            );

            System.out.println(
                    "Redis data:"
            );

            redis.opsForHash()
                    .entries(
                            "taskflow:task:" + taskId
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