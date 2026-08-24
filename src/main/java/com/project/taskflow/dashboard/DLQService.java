package com.project.taskflow.dashboard;

import com.project.taskflow.dashboard.dto.DLQEntryDTO;
import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.dlq.DeadLetterQueue;

import java.util.List;
import java.util.UUID;

public class DLQService {

    private final DeadLetterQueue deadLetterQueue;

    public DLQService(
            DeadLetterQueue deadLetterQueue) {

        this.deadLetterQueue =
                deadLetterQueue;
    }

    public List<DLQEntryDTO> getEntries() {

        return deadLetterQueue
                .getEntries()
                .stream()
                .map(DLQEntryDTO::new)
                .toList();
    }

    public DLQEntryDTO getEntry(
            UUID taskId) {

        DeadLetterEntry entry =
                deadLetterQueue.get(taskId);

        if (entry == null) {
            return null;
        }

        return new DLQEntryDTO(entry);
    }
}