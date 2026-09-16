import { api } from './client';
import { ApiKey, CreatedApiKeyResponse, CredentialEnvironment } from '../types';

export const credentialsApi = {
  getCredentials: (projectId: string) =>
    api.get<ApiKey[]>(`/api/projects/${projectId}/credentials`),

  createCredential: (
    projectId: string,
    name: string,
    environment: CredentialEnvironment
  ) =>
    api.post<CreatedApiKeyResponse>(`/api/projects/${projectId}/credentials`, {
      name,
      environment,
    }),

  revokeCredential: (projectId: string, credentialId: string) =>
    api.delete<void>(`/api/projects/${projectId}/credentials/${credentialId}`),
};
