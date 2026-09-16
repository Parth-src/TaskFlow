import { api } from './client';
import { Project } from '../types';

export const projectsApi = {
  getProjects: () => api.get<Project[]>('/api/projects'),

  getProject: (projectId: string) =>
    api.get<Project>(`/api/projects/${projectId}`),

  createProject: (name: string) =>
    api.post<Project>('/api/projects', { name }),

  connectRepository: (projectId: string, repository: string) =>
    api.post<Project>(`/api/projects/${projectId}/repository`, { repository }),
};
