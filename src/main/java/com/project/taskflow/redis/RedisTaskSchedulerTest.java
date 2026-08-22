package com.project.taskflow.redis;

import com.project.taskflow.queue.RedisTaskQueue;
import com.project.taskflow.queue.RedisTaskScheduler;
import com.project.taskflow.queue.TaskScheduler;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.UUID;

public class RedisTaskSchedulerTest {

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

            TaskScheduler scheduler =
                    new RedisTaskScheduler(queue);

            UUID taskId =
                    UUID.randomUUID();

            scheduler.schedule(
                    taskId,
                    System.currentTimeMillis()
            );

            System.out.println(
                    "Scheduled task: "
                            + taskId
            );

            List<UUID> next =
                    scheduler.nextTasks(10);

            System.out.println(
                    "Next task: "
                            + next
            );

            System.out.println(
                    "Queue size: "
                            + queue.size()
            );

        } finally {

            factory.destroy();
        }
    }
}