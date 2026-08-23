package com.project.taskflow.monitoring;

import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.dlq.RedisDeadLetterQueue;
import com.project.taskflow.execution.RedisExecutionStore;
import com.project.taskflow.queue.RedisTaskQueue;
import com.project.taskflow.queue.RedisTaskScheduler;
import com.project.taskflow.queue.TaskQueue;
import com.project.taskflow.queue.TaskScheduler;
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

    @Bean
    public WorkerRegistry workerRegistry() {

        return new WorkerRegistry();
    }

    @Bean
    public DeadLetterQueue deadLetterQueue(
            StringRedisTemplate redis) {

        return new RedisDeadLetterQueue(redis);
    }

    @Bean
    public TaskQueue taskQueue(
            StringRedisTemplate redis) {

        return new RedisTaskQueue(redis);
    }

    @Bean
    public TaskScheduler taskScheduler(
            TaskQueue queue) {

        return new RedisTaskScheduler(queue);
    }
}