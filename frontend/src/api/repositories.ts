import { api } from './client';
import { ConnectedRepository, GitHubRepository } from '../types';

export const repositoriesApi = {
  getConnectedRepositories: () =>
    api.get<ConnectedRepository[]>('/api/dashboard/repositories'),

  getGitHubRepositories: () =>
    api.get<GitHubRepository[]>('/api/dashboard/github/repositories'),
};
