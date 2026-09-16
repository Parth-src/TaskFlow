package com.project.taskflow.worker.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Standalone Java HTTP Worker Server.
 * Communicates with TaskFlow via HTTP on port 8081 with Bearer authentication,
 * receives JSON task parameters, executes Java business logic, and returns WorkerResponse.
 */
public class JavaWorkerServer {

    private final int port;
    private final String expectedToken;
    private HttpServer server;
    private final ObjectMapper mapper = new ObjectMapper();

    private final List<String> supportedWorkers = List.of(
            "validate",
            "payment",
            "inventory",
            "fulfillment",
            "send-email",
            "generate-invoice",
            "analytics",
            "event",
            "prepare",
            "send",
            "track",
            "extract",
            "transform",
            "load",
            "report",
            "fail-worker"
    );

    private final Map<String, AtomicInteger> attemptCounters = new ConcurrentHashMap<>();
    private final Map<String, Boolean> forceFailWorkers = new ConcurrentHashMap<>();
    private final Map<String, Object> lastReceivedParams = new ConcurrentHashMap<>();
    private final Map<String, String> lastReceivedRawBody = new ConcurrentHashMap<>();

    public JavaWorkerServer() {
        this(
                Integer.parseInt(System.getProperty("worker.port", System.getenv().getOrDefault("TASKFLOW_WORKER_PORT", "8081"))),
                System.getProperty("worker.token", System.getenv().getOrDefault("TASKFLOW_WORKER_TOKEN", "taskflow-worker-secret"))
        );
    }

    public JavaWorkerServer(int port, String expectedToken) {
        this.port = port;
        this.expectedToken = (expectedToken != null && !expectedToken.isBlank()) ? expectedToken : "taskflow-worker-secret";
    }

    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }

        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/", new RootHandler());

        server.start();
        System.out.println("[JavaWorkerServer] Started on port " + port + " (auth enabled)");
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("[JavaWorkerServer] Stopped.");
        }
    }

    public int getPort() {
        return port;
    }

    public Map<String, Object> getLastReceivedParams(String workerId) {
        Object val = lastReceivedParams.get(workerId);
        if (val instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) val;
            return map;
        }
        return null;
    }

    public String getLastReceivedRawBody(String workerId) {
        return lastReceivedRawBody.get(workerId);
    }

    public int getAttempts(String workerId) {
        AtomicInteger count = attemptCounters.get(workerId);
        return count != null ? count.get() : 0;
    }

    public void setForceFail(String workerId, boolean fail) {
        forceFailWorkers.put(workerId, fail);
    }

    public void reset() {
        attemptCounters.clear();
        forceFailWorkers.clear();
        lastReceivedParams.clear();
        lastReceivedRawBody.clear();
    }

    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod().toUpperCase();

            try {
                // Health endpoint
                if ("GET".equals(method) && (path.equals("/health") || path.equals("/health/"))) {
                    sendJsonResponse(exchange, 200, Map.of("status", "UP", "healthy", true));
                    return;
                }

                // Worker discovery endpoint
                if ("GET".equals(method) && (path.equals("/workers") || path.equals("/workers/"))) {
                    sendJsonResponse(exchange, 200, Map.of("workers", supportedWorkers));
                    return;
                }

                // Test Control: force failure
                if ("POST".equals(method) && path.startsWith("/control/fail/")) {
                    String workerId = path.substring("/control/fail/".length()).trim();
                    forceFailWorkers.put(workerId, true);
                    attemptCounters.put(workerId, new AtomicInteger(0));
                    sendJsonResponse(exchange, 200, Map.of(
                            "message", "Worker " + workerId + " set to fail mode",
                            "forceFail", true
                    ));
                    return;
                }

                // Test Control: force success
                if ("POST".equals(method) && path.startsWith("/control/succeed/")) {
                    String workerId = path.substring("/control/succeed/".length()).trim();
                    forceFailWorkers.put(workerId, false);
                    sendJsonResponse(exchange, 200, Map.of(
                            "message", "Worker " + workerId + " set to succeed mode",
                            "forceFail", false
                    ));
                    return;
                }

                // Test Control: stats
                if ("GET".equals(method) && path.startsWith("/control/stats/")) {
                    String workerId = path.substring("/control/stats/".length()).trim();
                    int attempts = getAttempts(workerId);
                    boolean forceFail = Boolean.TRUE.equals(forceFailWorkers.get(workerId));
                    sendJsonResponse(exchange, 200, Map.of(
                            "workerId", workerId,
                            "attempts", attempts,
                            "forceFail", forceFail
                    ));
                    return;
                }

                // Test Control: last received params
                if ("GET".equals(method) && path.startsWith("/control/last-received/")) {
                    String workerId = path.substring("/control/last-received/".length()).trim();
                    Object params = lastReceivedParams.get(workerId);
                    sendJsonResponse(exchange, 200, params != null ? params : Map.of());
                    return;
                }

                // Test Control: reset
                if ("POST".equals(method) && path.equals("/control/reset")) {
                    reset();
                    sendJsonResponse(exchange, 200, Map.of("success", true, "message", "Reset complete"));
                    return;
                }

                // Worker execution endpoint: POST /workers/{workerId}
                if ("POST".equals(method) && path.startsWith("/workers/")) {
                    handleWorkerExecution(exchange, path);
                    return;
                }

                sendJsonResponse(exchange, 404, Map.of("error", "Not found", "path", path));

            } catch (Exception e) {
                System.err.println("[JavaWorkerServer] Request error: " + e.getMessage());
                sendJsonResponse(exchange, 500, Map.of("error", "Internal server error: " + e.getMessage()));
            }
        }

        private void handleWorkerExecution(HttpExchange exchange, String path) throws IOException {
            String workerId = path.substring("/workers/".length()).trim();

            // Authentication verification
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ") || !authHeader.substring(7).trim().equals(expectedToken)) {
                sendJsonResponse(exchange, 401, Map.of(
                        "success", false,
                        "retry", false,
                        "message", "Worker authentication failed: Invalid or missing Bearer token"
                ));
                return;
            }

            // Read request body
            String body;
            try (InputStream is = exchange.getRequestBody()) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            lastReceivedRawBody.put(workerId, body);

            // Deserialize JSON parameters
            Object params = null;
            if (body != null && !body.isBlank()) {
                try {
                    params = mapper.readValue(body, Object.class);
                    lastReceivedParams.put(workerId, params);
                } catch (Exception e) {
                    System.err.println("[JavaWorkerServer] Failed to parse JSON body: " + e.getMessage());
                }
            } else {
                lastReceivedParams.put(workerId, Map.of());
            }

            AtomicInteger counter = attemptCounters.computeIfAbsent(workerId, k -> new AtomicInteger(0));
            int attemptNum = counter.incrementAndGet();

            System.out.println("[JavaWorkerServer] Received execution for worker: " + workerId
                    + " | Attempt #" + attemptNum + " | Params: " + (params != null ? params : "{}"));

            // Check if worker is set to fail
            Boolean forceFail = forceFailWorkers.get(workerId);
            boolean shouldFail = (forceFail != null) ? forceFail : "fail-worker".equals(workerId);
            if (shouldFail) {
                System.out.println("[JavaWorkerServer] Simulated failure for: " + workerId + " (attempt #" + attemptNum + ")");
                sendJsonResponse(exchange, 200, Map.of(
                        "success", false,
                        "retry", true,
                        "message", "Simulated worker failure for " + workerId + " (attempt #" + attemptNum + ")"
                ));
                return;
            }

            // Execute Java business logic
            String resultMessage = executeJavaWorkerLogic(workerId, params);

            sendJsonResponse(exchange, 200, Map.of(
                    "success", true,
                    "retry", false,
                    "message", resultMessage
            ));
        }

        private String executeJavaWorkerLogic(String workerId, Object params) {
            if (params instanceof Map<?, ?> map && map.containsKey("sleepMs")) {
                try {
                    long sleep = ((Number) map.get("sleepMs")).longValue();
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Simulated business logic execution in pure Java
            return switch (workerId) {
                case "payment" -> {
                    if (params instanceof Map<?, ?> map && map.containsKey("amount")) {
                        Object curr = map.get("currency");
                        String currency = curr != null ? curr.toString() : "USD";
                        yield "Payment of " + map.get("amount") + " " + currency + " processed successfully";
                    }
                    yield "Payment processed successfully";
                }
                case "generate-invoice" -> "Invoice generated successfully";
                case "send-email" -> {
                    if (params instanceof Map<?, ?> map && map.containsKey("email")) {
                        yield "Email dispatched to " + map.get("email");
                    }
                    yield "Email sent successfully";
                }
                case "analytics" -> "Analytics event recorded";
                case "validate" -> "Order validation completed";
                case "inventory" -> "Warehouse inventory reserved";
                case "fulfillment" -> "Order package scheduled for fulfillment";
                case "event" -> "Event ingested successfully";
                case "prepare" -> "Notification payload prepared";
                case "send" -> "Multi-channel message sent";
                case "track" -> "Delivery status tracked";
                case "extract" -> "Data extracted from source dataset";
                case "transform" -> "Data transformed according to schema";
                case "load" -> "Data loaded into destination table";
                case "report" -> "Executive report generated";
                default -> "Task " + workerId + " executed successfully";
            };
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object bodyObj) throws IOException {
            byte[] bytes = mapper.writeValueAsBytes(bodyObj);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }
        }
    }

    public static void main(String[] args) {
        try {
            JavaWorkerServer server = new JavaWorkerServer();
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("[JavaWorkerServer] Shutting down...");
                server.stop();
            }));

            System.out.println("[JavaWorkerServer] Worker application is running on port " + server.getPort());
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("[JavaWorkerServer] Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
