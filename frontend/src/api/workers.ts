import { api } from './client';
import { WorkerMetadata } from '../types';

export const workersApi = {
  getWorkers: () => api.get<WorkerMetadata[]>('/api/dashboard/workers'),
};
