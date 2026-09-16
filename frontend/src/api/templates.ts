import { api } from './client';
import { Template } from '../types';

export const templatesApi = {
  getTemplates: () => api.get<Template[]>('/api/dashboard/templates'),
};
