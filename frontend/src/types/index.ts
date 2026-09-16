export interface User {
  id: string;
  githubId: string;
  username: string;
  email?: string | null;
}

export interface AuthResponse {
  authenticated: boolean;
  user: User;
}

export interface Project {
  id: string;
  name: string;
  githubRepository?: string | null;
  createdAt: string;
}

export type CredentialEnvironment = 'DEVELOPMENT' | 'STAGING' | 'PRODUCTION';

export interface ApiKey {
  id: string;
  name: string;
  environment: CredentialEnvironment;
  createdAt: string;
  lastUsedAt?: string | null;
  revokedAt?: string | null;
}

export interface CreatedApiKeyResponse {
  id: string;
  name: string;
  environment: CredentialEnvironment;
  apiKey: string;
}

export interface WorkflowTask {
  id: string;
  worker: string;
  dependsOn: string[];
}

export interface WorkflowSummary {
  id: string;
  name: string;
  taskCount: number;
  tasks: WorkflowTask[];
}

export type ExecutionStatus = 'PENDING' | 'READY' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'RETRYING';

export interface ExecutionSummary {
  executionId: string;
  workflowId: string;
  status: ExecutionStatus | string;
  startedAt?: string | null;
  completedAt?: string | null;
  durationMs?: number | null;
}

export interface TaskExecution {
  taskId: string;
  workflowId: string;
  executionId: string;
  workerId: string;
  status: ExecutionStatus | string;
  attempt: number;
  createdAt: string;
  startedAt?: string | null;
  completedAt?: string | null;
  error?: string | null;
  durationMs?: number | null;
}

export interface WorkerMetadata {
  workerId: string;
  endpoint: string;
}

export interface DLQEntry {
  taskId: string;
  workerId: string;
  attemptCount: number;
  reason: string;
  timestamp: string;
}

export interface Template {
  id: string;
  name: string;
  description: string;
  category: string;
  taskCount: number;
  tasks: WorkflowTask[];
}

export interface ConnectedRepository {
  projectId: string;
  projectName: string;
  repository: string;
  createdAt: string;
}

export interface GitHubRepository {
  name: string;
  fullName: string;
  owner: string;
  description: string;
  defaultBranch: string;
  isPrivate: boolean;
}

export interface OverviewMetrics {
  totalProjects: number;
  totalWorkflows: number;
  runningExecutions: number;
  completedExecutions: number;
  failedExecutions: number;
  queuedTasks: number;
  activeWorkers: number;
  dlqCount: number;
}
