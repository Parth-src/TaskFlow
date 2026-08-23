package com.project.taskflow.queue;

import java.util.List;
import java.util.UUID;

public interface TaskScheduler {

    void schedule(
            UUID taskId,
            long availableAt
    );

    List<UUID> nextTasks(
            int maxTasks
    );

    void complete(
            UUID taskId
    );

    void recoverExpired();
}