package com.project.taskflow.redis;

import com.project.taskflow.queue.RedisTaskQueue;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.UUID;

public class RedisTaskQueueTest {

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

            RedisTaskQueue queue =
                    new RedisTaskQueue(redis);

            UUID taskId =
                    UUID.randomUUID();

            queue.add(
                    taskId,
                    System.currentTimeMillis()
            );

            System.out.println(
                    "Added task: " + taskId
            );

            System.out.println(
                    "Queue size: "
                            + queue.size()
            );

            List<UUID> results =
                    queue.poll(10);

            System.out.println(
                    "Polled tasks: " + results
            );

            System.out.println(
                    "Queue size: " + queue.size()
            );

        } finally {

            factory.destroy();
        }
    }
}