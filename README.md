# TaskFlow

### Distributed Workflow Orchestration Platform

TaskFlow is a backend-focused workflow orchestration platform for executing **long-running and background tasks asynchronously**.

Instead of making an application wait for expensive operations to finish inside an API request, TaskFlow accepts the work, manages its execution in the background, and dispatches individual tasks to **language-independent workers**.

```text
Application
     |
     | Submit workflow
     v
+----------------------+
|       TaskFlow       |
|   Spring Boot API    |
+----------+-----------+
           |
           | Background execution
           v
+----------------------+
|   Workflow Engine    |
| Dependencies / State |
+----------+-----------+
           |
           v
+----------------------+
|     Redis Queue      |
+----------+-----------+
           |
           | HTTP dispatch
           v
+----------------------+
| Language-Independent |
|       Worker         |
+----------+-----------+
           |
           v
      Result / Failure
           |
       +---+---+
       |       |
    Success   Retry
               |
               v
              DLQ
```

## Core Features

### 1. Asynchronous Background Execution

TaskFlow separates API request handling from background task execution.

An application can submit work to TaskFlow and receive a **fast API response** without waiting for long-running tasks to complete.

```text
Client
  |
  | Submit workflow
  v
Application
  |
  | TaskFlow API
  v
TaskFlow
  |
  +--------------------> Fast API response
  |
  +--------------------> Background execution
                              |
                              v
                         Worker
```

---

### 2. Workflow Execution

A workflow represents a collection of tasks that need to be executed as part of a larger operation.

```text
payment
   |
   v
generate-invoice
   |
   v
analytics
   |
   v
send-email
```

TaskFlow manages the execution of these tasks while the actual business logic remains inside the worker.

---

### 3. Task Dependencies

Tasks can depend on the successful completion of other tasks.

```text
payment
   |
   +-------> generate-invoice
   |
   +-------> analytics
                 |
                 v
            send-email
```

Dependent tasks are executed only when their required predecessor tasks have completed successfully.

This allows TaskFlow to represent **sequential and parallel workflow execution**.

---

### 4. Language-Independent Workers

TaskFlow separates **workflow orchestration from task execution**.

Workers can be implemented independently of the orchestration backend and communicate with TaskFlow through the worker protocol.

```text
                         TaskFlow
                            |
                       HTTP Dispatch
                            |
          +-----------------+-----------------+
          |                 |                 |
          v                 v                 v
     Java Worker       Python Worker      Node Worker
```

The worker is responsible for executing the business logic, while TaskFlow manages:

* task dispatch
* workflow dependencies
* execution state
* retries
* failure handling
* Dead Letter Queue processing

This keeps the orchestration layer independent of the programming language used to implement the worker.

---

### 5. Retry Handling

Failed background tasks can be retried instead of immediately being marked as permanently failed.

```text
Task
 |
 v
Attempt 1
 |
 +---- Success ----> Complete
 |
 +---- Failure
       |
       v
     Retry
       |
       v
   Attempt 2
       |
       +---- Success ----> Complete
       |
       +---- Failure ----> DLQ
```

---

### 6. Dead Letter Queue

Tasks that continue to fail after retry attempts are moved to a **Dead Letter Queue (DLQ)**.

TaskFlow preserves failure information such as:

```text
Task
Execution ID
Attempt Count
Failure Reason
Timestamp
Worker Information
```

Redis is used for fast queue and execution-state operations.

---

## Architecture

```text
                    +----------------+
                    |   Application  |
                    +-------+--------+
                            |
                            | API
                            v
                 +-----------------------+
                 |       TaskFlow        |
                 |     Spring Boot       |
                 +-----------+-----------+
                             |
              +--------------+--------------+
              |                             |
              v                             v
       +-------------+               +-------------+
       | PostgreSQL  |               |    Redis    |
       |             |               |             |
       | Projects    |               | Queue       |
       | Workflows   |               | State       |
       | Executions  |               | DLQ         |
       +-------------+               +------+------+
                                            |
                                            | HTTP
                                            v
                              +--------------------------+
                              | Language-Independent     |
                              |          Worker          |
                              +--------------------------+
                                            |
                                            v
                                   Business Logic
```

## Technology Stack

| Component               | Technology               |
| ----------------------- | ------------------------ |
| Backend                 | Java                     |
| Framework               | Spring Boot              |
| Database                | PostgreSQL               |
| Execution / Queue State | Redis                    |
| Worker Protocol         | HTTP                     |
| API                     | REST / HTTP              |
| Authentication          | API Keys + Worker Tokens |
| Build                   | Maven                    |

## Local Integration

The local system consists of:

```text
PostgreSQL
    |
Redis
    |
TaskFlow Backend :8080
    |
Language-Independent Worker
    |
Application / SDK
```

The current worker integration can be tested using the existing Node.js worker implementation, but **Node.js is only an example worker runtime** and is not a requirement of TaskFlow's architecture.

### End-to-End Flow

```text
Application
    |
    | Submit Workflow
    v
TaskFlow API
    |
    | Return execution ID
    v
Fast API Response
    |
    | Background execution
    v
Workflow Engine
    |
    | Resolve dependencies
    v
Redis
    |
    | Dispatch task
    v
Language-Independent Worker
    |
    | Execute business logic
    v
Success / Failure
    |
    +----> Next dependent task
    |
    +----> Retry
    |
    +----> DLQ
```

The core principle is:

> **TaskFlow handles when and how work executes; workers handle what the work actually does.**
