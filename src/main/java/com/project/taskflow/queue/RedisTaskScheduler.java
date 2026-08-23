package com.project.taskflow.queue;

import java.util.List;
import java.util.UUID;

public class RedisTaskScheduler
        implements TaskScheduler {

    private final TaskQueue queue;

    public RedisTaskScheduler(
            TaskQueue queue) {

        this.queue = queue;
    }


    @Override
    public void schedule(
            UUID taskId,
            long availableAt) {

        queue.add(
                taskId,
                availableAt
        );
    }


    @Override
    public List<UUID> nextTasks(
            int maxTasks) {

        return queue.poll(
                maxTasks
        );
    }


    @Override
    public void complete(
            UUID taskId) {

        queue.complete(
                taskId
        );
    }


    @Override
    public void recoverExpired() {

        queue.recoverExpired();
    }
}