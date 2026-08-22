package com.project.taskflow.redis;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class RedisConnectionTest {

    public static void main(String[] args) {

        LettuceConnectionFactory factory =
                new LettuceConnectionFactory(
                        "localhost",
                        6379
                );

        factory.afterPropertiesSet();

        try (RedisConnection connection =
                     factory.getConnection()) {

            String response =
                    connection.ping();

            System.out.println(
                    "Redis response: "
                            + response
            );

        } finally {

            factory.destroy();
        }
    }
}