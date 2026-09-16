import { api } from './client';
import { OverviewMetrics } from '../types';

export const overviewApi = {
  getOverview: (projectId?: string) => {
    const params = new URLSearchParams();
    if (projectId) params.append('projectId', projectId);
    const query = params.toString() ? `?${params.toString()}` : '';
    return api.get<OverviewMetrics>(`/api/dashboard/overview${query}`);
  },
};
