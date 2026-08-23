package com.project.taskflow.queue;

import java.util.List;
import java.util.UUID;

public interface TaskQueue {

    void add(
            UUID taskId,
            long availableAt
    );

    List<UUID> poll(
            int maxTasks
    );

    long size();

    void complete(
            UUID taskId
    );

    void recoverExpired();
}