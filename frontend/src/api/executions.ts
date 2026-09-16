import { api } from './client';
import { ExecutionSummary, TaskExecution } from '../types';

export const executionsApi = {
  getRecentExecutions: (projectId?: string, limit: number = 20) => {
    const params = new URLSearchParams();
    if (projectId) params.append('projectId', projectId);
    params.append('limit', limit.toString());
    return api.get<ExecutionSummary[]>(`/api/dashboard/executions?${params.toString()}`);
  },

  getExecution: (executionId: string, projectId?: string) => {
    const params = new URLSearchParams();
    if (projectId) params.append('projectId', projectId);
    const query = params.toString() ? `?${params.toString()}` : '';
    return api.get<TaskExecution[]>(`/api/dashboard/executions/${executionId}${query}`);
  },
};
