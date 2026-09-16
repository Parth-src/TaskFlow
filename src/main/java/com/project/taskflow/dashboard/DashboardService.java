package com.project.taskflow.dashboard;

import com.project.taskflow.auth.GitHubOAuthService;
import com.project.taskflow.dashboard.dto.*;
import com.project.taskflow.dlq.DeadLetterEntry;
import com.project.taskflow.dlq.DeadLetterQueue;
import com.project.taskflow.execution.ExecutionEngine;
import com.project.taskflow.execution.ExecutionStore;
import com.project.taskflow.execution.TaskExecution;
import com.project.taskflow.execution.WorkflowExecutor;
import com.project.taskflow.model.Workflow;
import com.project.taskflow.project.Project;
import com.project.taskflow.project.ProjectAuthorizationService;
import com.project.taskflow.project.ProjectRepository;
import com.project.taskflow.queue.TaskScheduler;
import com.project.taskflow.retry.RetryPolicy;
import com.project.taskflow.user.User;
import com.project.taskflow.user.UserRepository;
import com.project.taskflow.worker.WorkerMetadata;
import com.project.taskflow.worker.WorkerRegistry;
import com.project.taskflow.workflow.TaskDefinition;
import com.project.taskflow.workflow.WorkflowDefinition;
import com.project.taskflow.workflow.WorkflowLoader;
import com.project.taskflow.workflow.WorkflowParser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Service
public class DashboardService {

    private final WorkerRegistry workerRegistry;
    private final ExecutionStore executionStore;
    private final DeadLetterQueue deadLetterQueue;
    private final TaskScheduler taskScheduler;
    private final ProjectRepository projectRepository;
    private final ProjectAuthorizationService authorizationService;
    private final UserRepository userRepository;
    private final GitHubOAuthService githubOAuthService;
    private final String workerToken;
    private final String workerHost;

    private final WorkflowParser workflowParser = new WorkflowParser();
    private final WorkflowLoader workflowLoader = new WorkflowLoader();
    private final Map<String, WorkflowDefinition> workflowDefinitions = new ConcurrentHashMap<>();

    public DashboardService(
            WorkerRegistry workerRegistry,
            ExecutionStore executionStore,
            DeadLetterQueue deadLetterQueue,
            TaskScheduler taskScheduler,
            ProjectRepository projectRepository,
            ProjectAuthorizationService authorizationService,
            UserRepository userRepository,
            GitHubOAuthService githubOAuthService,
            @Value("${taskflow.worker.token:${TASKFLOW_WORKER_TOKEN:taskflow-worker-secret}}") String workerToken,
            @Value("${taskflow.worker.host:http://127.0.0.1:8081}") String workerHost) {

        this.workerRegistry = workerRegistry;
        this.executionStore = executionStore;
        this.deadLetterQueue = deadLetterQueue;
        this.taskScheduler = taskScheduler;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.githubOAuthService = githubOAuthService;
        this.workerToken = (workerToken != null && !workerToken.isBlank()) ? workerToken : "taskflow-worker-secret";
        this.workerHost = (workerHost != null && !workerHost.isBlank()) ? workerHost : "http://127.0.0.1:8081";

        loadWorkflowsFromClasspath();
        registerDefaultWorkersIfEmpty();
    }

    private void registerDefaultWorkersIfEmpty() {
        if (workerRegistry.getAll().isEmpty()) {
            workerRegistry.register("payment", workerHost + "/workers/payment");
            workerRegistry.register("send-email", workerHost + "/workers/send-email");
            workerRegistry.register("generate-invoice", workerHost + "/workers/generate-invoice");
            workerRegistry.register("analytics", workerHost + "/workers/analytics");
            workerRegistry.register("validate", workerHost + "/workers/validate");
            workerRegistry.register("inventory", workerHost + "/workers/inventory");
            workerRegistry.register("fulfillment", workerHost + "/workers/fulfillment");
            workerRegistry.register("event", workerHost + "/workers/event");
            workerRegistry.register("prepare", workerHost + "/workers/prepare");
            workerRegistry.register("send", workerHost + "/workers/send");
            workerRegistry.register("track", workerHost + "/workers/track");
            workerRegistry.register("extract", workerHost + "/workers/extract");
            workerRegistry.register("transform", workerHost + "/workers/transform");
            workerRegistry.register("load", workerHost + "/workers/load");
            workerRegistry.register("report", workerHost + "/workers/report");
        }
    }

    private void loadWorkflowsFromClasspath() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:workflows/*.yaml");
            for (Resource resource : resources) {
                try (InputStream is = resource.getInputStream()) {
                    WorkflowDefinition def = workflowParser.parse(is);
                    if (def != null && def.getName() != null) {
                        workflowDefinitions.put(def.getName(), def);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load workflows from classpath: " + e.getMessage());
        }
    }

    public List<WorkerMetadata> getWorkers() {
        registerDefaultWorkersIfEmpty();
        return new ArrayList<>(workerRegistry.getAll());
    }

    public List<TaskExecutionDTO> getExecution(
            UUID projectId,
            UUID userId,
            String executionId) {

        authorizationService.getOwnedProject(projectId, userId);

        List<TaskExecution> executions =
                executionStore.getByExecutionId(executionId);

        if (executions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Execution not found"
            );
        }

        for (TaskExecution execution : executions) {
            if (projectId != null && !projectId.equals(execution.getProjectId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "You do not have access to this execution"
                );
            }
        }

        return executions
                .stream()
                .map(TaskExecutionDTO::new)
                .toList();
    }

    public List<TaskExecutionDTO> getExecutionForUser(
            UUID userId,
            String executionId) {

        List<TaskExecution> executions =
                executionStore.getByExecutionId(executionId);

        if (executions.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Execution not found"
            );
        }

        UUID taskProjectId = executions.get(0).getProjectId();
        if (taskProjectId != null) {
            authorizationService.getOwnedProject(taskProjectId, userId);
        }

        return executions
                .stream()
                .map(TaskExecutionDTO::new)
                .toList();
    }

    public List<ExecutionSummaryDTO> getRecentExecutions(
            UUID projectId,
            UUID userId,
            int limit) {

        authorizationService.getOwnedProject(projectId, userId);

        if (limit <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Limit must be greater than zero"
            );
        }

        if (limit > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Limit cannot exceed 100"
            );
        }

        return executionStore
                .getRecentExecutions(projectId, limit)
                .stream()
                .map(ExecutionSummaryDTO::new)
                .toList();
    }

    public List<ExecutionSummaryDTO> getRecentExecutionsForUser(
            UUID userId,
            int limit) {

        List<Project> userProjects = projectRepository.findByUserId(userId);
        if (userProjects.isEmpty()) {
            return List.of();
        }

        List<ExecutionSummaryDTO> all = new ArrayList<>();
        for (Project project : userProjects) {
            List<TaskExecution> recent = executionStore.getRecentExecutions(project.getId(), limit);
            for (TaskExecution exec : recent) {
                all.add(new ExecutionSummaryDTO(exec));
            }
        }

        all.sort((a, b) -> {
            Instant t1 = a.getStartedAt() != null ? a.getStartedAt() : Instant.EPOCH;
            Instant t2 = b.getStartedAt() != null ? b.getStartedAt() : Instant.EPOCH;
            return t2.compareTo(t1);
        });

        return all.size() > limit ? all.subList(0, limit) : all;
    }

    public OverviewDTO getOverview(
            UUID userId,
            UUID projectId) {

        List<Project> projects = projectRepository.findByUserId(userId);
        int totalProjects = projects.size();
        int totalWorkflows = workflowDefinitions.size();

        int running = 0;
        int completed = 0;
        int failed = 0;
        int queued = 0;

        List<Project> targetProjects = (projectId != null)
                ? List.of(authorizationService.getOwnedProject(projectId, userId))
                : projects;

        for (Project proj : targetProjects) {
            List<TaskExecution> recents = executionStore.getRecentExecutions(proj.getId(), 100);
            for (TaskExecution summary : recents) {
                List<TaskExecution> tasks = executionStore.getByExecutionId(summary.getExecutionId());
                boolean hasRunning = false;
                boolean hasFailed = false;
                boolean allCompleted = !tasks.isEmpty();

                for (TaskExecution t : tasks) {
                    if ("RUNNING".equalsIgnoreCase(t.getStatus())) {
                        hasRunning = true;
                    } else if ("FAILED".equalsIgnoreCase(t.getStatus())) {
                        hasFailed = true;
                    } else if ("PENDING".equalsIgnoreCase(t.getStatus()) || "READY".equalsIgnoreCase(t.getStatus())) {
                        queued++;
                    }
                    if (!"COMPLETED".equalsIgnoreCase(t.getStatus())) {
                        allCompleted = false;
                    }
                }

                if (hasRunning) {
                    running++;
                } else if (hasFailed) {
                    failed++;
                } else if (allCompleted) {
                    completed++;
                }
            }
        }

        registerDefaultWorkersIfEmpty();
        int activeWorkers = workerRegistry.getAll().size();
        int dlqCount = deadLetterQueue.getEntries().size();

        return new OverviewDTO(
                totalProjects,
                totalWorkflows,
                running,
                completed,
                failed,
                queued,
                activeWorkers,
                dlqCount
        );
    }

    public List<WorkflowSummaryDTO> getWorkflows() {
        if (workflowDefinitions.isEmpty()) {
            loadWorkflowsFromClasspath();
        }

        List<WorkflowSummaryDTO> list = new ArrayList<>();
        for (WorkflowDefinition def : workflowDefinitions.values()) {
            List<WorkflowTaskDTO> tasks = new ArrayList<>();
            if (def.getTasks() != null) {
                for (TaskDefinition td : def.getTasks()) {
                    tasks.add(new WorkflowTaskDTO(td.getId(), td.getWorker(), td.getDependsOn(), td.getParams()));
                }
            }
            list.add(new WorkflowSummaryDTO(def.getName(), def.getName(), tasks));
        }

        return list;
    }

    public WorkflowSummaryDTO getWorkflow(String workflowId) {
        if (workflowDefinitions.isEmpty()) {
            loadWorkflowsFromClasspath();
        }

        WorkflowDefinition def = workflowDefinitions.get(workflowId);
        if (def == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + workflowId);
        }

        List<WorkflowTaskDTO> tasks = new ArrayList<>();
        if (def.getTasks() != null) {
            for (TaskDefinition td : def.getTasks()) {
                tasks.add(new WorkflowTaskDTO(td.getId(), td.getWorker(), td.getDependsOn(), td.getParams()));
            }
        }

        return new WorkflowSummaryDTO(def.getName(), def.getName(), tasks);
    }

    public Map<String, Object> executeWorkflow(
            UUID projectId,
            UUID userId,
            String workflowId) {

        authorizationService.getOwnedProject(projectId, userId);

        if (workflowDefinitions.isEmpty()) {
            loadWorkflowsFromClasspath();
        }

        WorkflowDefinition definition = workflowDefinitions.get(workflowId);
        if (definition == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found: " + workflowId);
        }

        Workflow workflow = workflowLoader.load(definition);
        ExecutionEngine engine = new ExecutionEngine(workflow);
        RetryPolicy retryPolicy = new RetryPolicy(3);

        WorkflowExecutor executor = new WorkflowExecutor(
                engine,
                workerRegistry,
                retryPolicy,
                deadLetterQueue,
                taskScheduler,
                executionStore,
                workerToken,
                projectId
        );

        String executionId = UUID.randomUUID().toString();
        Instant createdAt = Instant.now();

        // Asynchronously dispatch the background workflow execution
        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            try {
                executor.execute(executionId);
            } catch (Exception e) {
                System.err.println("Async workflow execution failed: " + e.getMessage());
            }
        });

        Map<String, Object> response = new HashMap<>();
        response.put("executionId", executionId);
        response.put("workflowId", workflowId);
        response.put("status", "QUEUED");
        response.put("projectId", projectId);
        response.put("createdAt", createdAt.toString());

        return response;
    }

    public void reprocessDLQTask(
            UUID projectId,
            UUID userId,
            UUID taskId) {

        authorizationService.getOwnedProject(projectId, userId);

        DeadLetterEntry entry = deadLetterQueue.get(taskId);
        if (entry == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DLQ entry not found: " + taskId);
        }

        WorkflowDefinition def = workflowDefinitions.values().stream().findFirst().orElse(null);
        Workflow workflow = def != null ? workflowLoader.load(def) : new Workflow("reprocess");
        ExecutionEngine engine = new ExecutionEngine(workflow);
        RetryPolicy retryPolicy = new RetryPolicy(3);

        WorkflowExecutor executor = new WorkflowExecutor(
                engine,
                workerRegistry,
                retryPolicy,
                deadLetterQueue,
                taskScheduler,
                executionStore,
                workerToken,
                projectId
        );

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> {
            try {
                executor.reprocess(taskId);
            } catch (Exception e) {
                System.err.println("DLQ reprocessing failed: " + e.getMessage());
            }
        });
    }

    public List<TemplateDTO> getTemplates() {
        return List.of(
                new TemplateDTO(
                        "payment-processing",
                        "Payment Processing Flow",
                        "Orchestrates credit card billing, automated invoice generation, transactional customer email, and sales telemetry.",
                        "Fintech & Billing",
                        List.of(
                                new WorkflowTaskDTO("payment", "payment", List.of()),
                                new WorkflowTaskDTO("send-email", "send-email", List.of("payment")),
                                new WorkflowTaskDTO("generate-invoice", "generate-invoice", List.of("payment")),
                                new WorkflowTaskDTO("analytics", "analytics", List.of("payment"))
                        )
                ),
                new TemplateDTO(
                        "order-fulfillment",
                        "E-Commerce Order Fulfillment",
                        "Validates cart checkout, captures payment, reserves warehouse inventory, and dispatches fulfillment logistics.",
                        "E-Commerce",
                        List.of(
                                new WorkflowTaskDTO("validate", "validate", List.of()),
                                new WorkflowTaskDTO("payment", "payment", List.of("validate")),
                                new WorkflowTaskDTO("inventory", "inventory", List.of("payment")),
                                new WorkflowTaskDTO("fulfillment", "fulfillment", List.of("inventory"))
                        )
                ),
                new TemplateDTO(
                        "notification-pipeline",
                        "Omnichannel Notification Pipeline",
                        "Ingests user events, prepares personalized templates, dispatches multi-channel alerts, and tracks engagement metrics.",
                        "Communication",
                        List.of(
                                new WorkflowTaskDTO("event", "event", List.of()),
                                new WorkflowTaskDTO("prepare", "prepare", List.of("event")),
                                new WorkflowTaskDTO("send", "send", List.of("prepare")),
                                new WorkflowTaskDTO("track", "track", List.of("send"))
                        )
                ),
                new TemplateDTO(
                        "data-etl-pipeline",
                        "Data Processing & ETL",
                        "Extracts raw batch datasets, transforms structure according to schema, loads into analytical data warehouse, and generates executive reporting.",
                        "Data Engineering",
                        List.of(
                                new WorkflowTaskDTO("extract", "extract", List.of()),
                                new WorkflowTaskDTO("transform", "transform", List.of("extract")),
                                new WorkflowTaskDTO("load", "load", List.of("transform")),
                                new WorkflowTaskDTO("report", "report", List.of("load"))
                        )
                )
        );
    }

    public List<ConnectedRepositoryDTO> getConnectedRepositories(UUID userId) {
        List<Project> projects = projectRepository.findByUserId(userId);
        List<ConnectedRepositoryDTO> repos = new ArrayList<>();
        for (Project project : projects) {
            if (project.getGithubRepository() != null && !project.getGithubRepository().isBlank()) {
                repos.add(new ConnectedRepositoryDTO(
                        project.getId(),
                        project.getName(),
                        project.getGithubRepository(),
                        project.getCreatedAt()
                ));
            }
        }
        return repos;
    }

    public List<GitHubRepositoryDTO> getGitHubRepositories(UUID userId) {
        if (userId == null) {
            return List.of();
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getGithubAccessToken() == null || user.getGithubAccessToken().isBlank()) {
            return List.of();
        }

        return githubOAuthService.fetchUserRepositories(user.getGithubAccessToken());
    }
}