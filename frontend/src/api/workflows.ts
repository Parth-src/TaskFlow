import { api } from './client';
import { WorkflowSummary } from '../types';

export interface TriggerExecutionResponse {
  executionId: string;
  workflowId: string;
  status: string;
  projectId: string;
  createdAt: string;
}

export const workflowsApi = {
  getWorkflows: () => api.get<WorkflowSummary[]>('/api/dashboard/workflows'),

  getWorkflow: (workflowId: string) =>
    api.get<WorkflowSummary>(`/api/dashboard/workflows/${workflowId}`),

  executeWorkflow: (workflowId: string, projectId: string) =>
    api.post<TriggerExecutionResponse>(
      `/api/dashboard/workflows/${workflowId}/execute?projectId=${projectId}`
    ),
};
