package com.project.taskflow.dashboard;

import com.project.taskflow.dashboard.dto.DLQEntryDTO;
import com.project.taskflow.dlq.DeadLetterQueue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/dlq")
public class DLQController {

    private final DLQService dlqService;

    public DLQController(
            DeadLetterQueue deadLetterQueue) {

        this.dlqService =
                new DLQService(
                        deadLetterQueue
                );
    }

    @GetMapping
    public List<DLQEntryDTO> getEntries() {

        return dlqService.getEntries();
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<DLQEntryDTO> getEntry(
            @PathVariable UUID taskId) {

        DLQEntryDTO entry =
                dlqService.getEntry(taskId);

        if (entry == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(entry);
    }
}