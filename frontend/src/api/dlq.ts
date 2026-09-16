import { api } from './client';
import { DLQEntry } from '../types';

export const dlqApi = {
  getEntries: () => api.get<DLQEntry[]>('/api/dashboard/dlq'),

  getEntry: (taskId: string) =>
    api.get<DLQEntry>(`/api/dashboard/dlq/${taskId}`),

  reprocessTask: (taskId: string, projectId: string) =>
    api.post<{ success: boolean; message: string }>(
      `/api/dashboard/dlq/${taskId}/reprocess?projectId=${projectId}`
    ),
};
