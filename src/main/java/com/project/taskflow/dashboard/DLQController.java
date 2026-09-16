package com.project.taskflow.dashboard;

import com.project.taskflow.auth.UserContext;
import com.project.taskflow.dashboard.dto.DLQEntryDTO;
import com.project.taskflow.dlq.DeadLetterQueue;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard/dlq")
public class DLQController {

    private final DLQService dlqService;
    private final DashboardService dashboardService;

    public DLQController(
            DeadLetterQueue deadLetterQueue,
            DashboardService dashboardService) {

        this.dlqService =
                new DLQService(
                        deadLetterQueue
                );
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public List<DLQEntryDTO> getEntries() {

        UserContext.require();
        return dlqService.getEntries();
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<DLQEntryDTO> getEntry(
            @PathVariable UUID taskId) {

        UserContext.require();
        DLQEntryDTO entry =
                dlqService.getEntry(taskId);

        if (entry == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        return ResponseEntity.ok(entry);
    }

    @PostMapping("/{taskId}/reprocess")
    public Map<String, Object> reprocess(
            @PathVariable UUID taskId,
            @RequestParam UUID projectId) {

        UUID userId = UserContext.require();
        dashboardService.reprocessDLQTask(projectId, userId, taskId);

        return Map.of(
                "success", true,
                "message", "Task reprocessing initiated for " + taskId
        );
    }
}