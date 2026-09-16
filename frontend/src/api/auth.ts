import { api } from './client';
import { AuthResponse } from '../types';

export const authApi = {
  getMe: () => api.get<AuthResponse>('/api/auth/me'),

  logout: () => api.post<{ success: boolean }>('/api/auth/logout'),

  devLogin: (username?: string, email?: string) =>
    api.post<AuthResponse>('/api/auth/dev-login', { username, email }),

  getGitHubAuthUrl: () => {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
    return `${baseUrl}/api/auth/github`;
  },
};
