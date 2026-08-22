package com.project.taskflow.monitoring;

import com.project.taskflow.execution.RedisExecutionStore;
import com.project.taskflow.worker.WorkerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class MonitoringConfig {

    @Bean
    public RedisExecutionStore executionStore(
            StringRedisTemplate redis) {

        return new RedisExecutionStore(
                redis
        );
    }

    @Bean
    public ExecutionMonitor executionMonitor(
            RedisExecutionStore store) {

        return new RedisExecutionMonitor(
                store
        );
    }

    @Bean
    public WorkerMonitor workerMonitor(
            WorkerRegistry registry) {

        return new HttpWorkerMonitor(
                registry
        );
    }
}