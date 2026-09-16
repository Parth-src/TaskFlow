import React, { useEffect, useState, useCallback } from 'react';
import { useProject } from '../context/ProjectContext';
import { credentialsApi } from '../api/credentials';
import { ApiKey, CredentialEnvironment, CreatedApiKeyResponse } from '../types';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Modal } from '../components/common/Modal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { EmptyState } from '../components/common/EmptyState';
import { CodeBlock } from '../components/common/CodeBlock';
import {
  KeyRound,
  Plus,
  Trash2,
  RotateCcw,
  CheckCircle2,
  AlertTriangle,
  Shield,
  Copy,
} from 'lucide-react';

export const CredentialsPage: React.FC = () => {
  const { activeProject } = useProject();
  const [credentials, setCredentials] = useState<ApiKey[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Generate Key Modal
  const [generateModalOpen, setGenerateModalOpen] = useState(false);
  const [keyName, setKeyName] = useState('');
  const [keyEnv, setKeyEnv] = useState<CredentialEnvironment>('DEVELOPMENT');
  const [generating, setGenerating] = useState(false);

  // One-time Revealed Secret Modal
  const [revealedKey, setRevealedKey] = useState<CreatedApiKeyResponse | null>(null);

  const loadCredentials = useCallback(async () => {
    if (!activeProject) return;

    try {
      setLoading(true);
      setError(null);
      const data = await credentialsApi.getCredentials(activeProject.id);
      setCredentials(data || []);
    } catch (err: any) {
      console.error('Failed to load credentials:', err);
      setError(err?.message || 'Failed to load project credentials');
    } finally {
      setLoading(false);
    }
  }, [activeProject]);

  useEffect(() => {
    loadCredentials();
  }, [loadCredentials]);

  const handleGenerate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!activeProject || !keyName.trim()) return;

    try {
      setGenerating(true);
      setError(null);
      const res = await credentialsApi.createCredential(
        activeProject.id,
        keyName.trim(),
        keyEnv
      );
      setRevealedKey(res);
      setGenerateModalOpen(false);
      setKeyName('');
      await loadCredentials();
    } catch (err: any) {
      setError(err?.message || 'Failed to generate API key');
    } finally {
      setGenerating(false);
    }
  };

  const handleRevoke = async (credentialId: string) => {
    if (!activeProject) return;
    if (!window.confirm('Are you sure you want to revoke this API key? This action is immediate and cannot be undone.')) {
      return;
    }

    try {
      await credentialsApi.revokeCredential(activeProject.id, credentialId);
      await loadCredentials();
    } catch (err: any) {
      setError(err?.message || 'Failed to revoke credential');
    }
  };

  if (!activeProject) {
    return (
      <EmptyState
        icon={KeyRound}
        title="No active project selected"
        description="Select a project from the top bar to view and manage its API credentials."
      />
    );
  }

  if (loading && credentials.length === 0) {
    return <LoadingSpinner message="Querying credentials from PostgreSQL store..." />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <KeyRound size={22} color="var(--cyan-400)" />
            <span>Project API Credentials</span>
          </h1>
          <p className="page-subtitle">
            Manage authentication keys for CLI execution and SDK submission for{' '}
            <strong style={{ color: 'var(--text-primary)' }}>{activeProject.name}</strong>.
          </p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <Button
            variant="secondary"
            size="sm"
            icon={<RotateCcw size={13} />}
            onClick={loadCredentials}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            icon={<Plus size={14} />}
            onClick={() => setGenerateModalOpen(true)}
          >
            Generate API Key
          </Button>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '16px' }}>
          <ErrorState message={error} onRetry={loadCredentials} />
        </div>
      )}

      {credentials.length === 0 ? (
        <EmptyState
          icon={KeyRound}
          title="No API credentials generated yet"
          description="Generate a project API key to authorize SDKs and CLI background workflow dispatches."
          action={
            <Button
              variant="primary"
              onClick={() => setGenerateModalOpen(true)}
              icon={<Plus size={14} />}
            >
              Generate First API Key
            </Button>
          }
        />
      ) : (
        <Card
          title={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Shield size={16} color="var(--cyan-400)" />
              <span>Active Credentials ({credentials.length})</span>
            </div>
          }
          subtitle="TaskFlow stores only cryptographic hashes in PostgreSQL; raw secrets are never displayed again."
        >
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Credential Name</th>
                  <th>Environment</th>
                  <th>Created At</th>
                  <th>Last Used</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {credentials.map((cred) => {
                  const isRevoked = cred.revokedAt != null;
                  return (
                    <tr key={cred.id}>
                      <td>
                        <strong style={{ color: 'var(--text-primary)' }}>{cred.name}</strong>
                        <div className="font-mono" style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                          ID: {cred.id}
                        </div>
                      </td>
                      <td>
                        <span
                          style={{
                            fontSize: '11px',
                            fontFamily: 'var(--font-mono)',
                            padding: '2px 8px',
                            borderRadius: '4px',
                            background:
                              cred.environment === 'PRODUCTION'
                                ? 'rgba(244, 63, 94, 0.15)'
                                : cred.environment === 'STAGING'
                                ? 'rgba(245, 158, 11, 0.15)'
                                : 'rgba(6, 182, 212, 0.15)',
                            color:
                              cred.environment === 'PRODUCTION'
                                ? 'var(--rose-400)'
                                : cred.environment === 'STAGING'
                                ? 'var(--amber-400)'
                                : 'var(--cyan-400)',
                            fontWeight: 600,
                          }}
                        >
                          {cred.environment}
                        </span>
                      </td>
                      <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                        {new Date(cred.createdAt).toLocaleDateString()}
                      </td>
                      <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                        {cred.lastUsedAt ? new Date(cred.lastUsedAt).toLocaleString() : 'Never'}
                      </td>
                      <td>
                        {isRevoked ? (
                          <span className="badge badge-failed">REVOKED</span>
                        ) : (
                          <span className="badge badge-completed">ACTIVE</span>
                        )}
                      </td>
                      <td>
                        {!isRevoked && (
                          <Button
                            variant="danger"
                            size="sm"
                            icon={<Trash2 size={12} />}
                            onClick={() => handleRevoke(cred.id)}
                          >
                            Revoke
                          </Button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      {/* Generate Key Modal */}
      <Modal
        isOpen={generateModalOpen}
        onClose={() => setGenerateModalOpen(false)}
        title="Generate Project API Key"
        footer={
          <div style={{ display: 'flex', gap: '8px' }}>
            <Button variant="secondary" onClick={() => setGenerateModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              loading={generating}
              onClick={handleGenerate}
              disabled={!keyName.trim()}
            >
              Generate Key
            </Button>
          </div>
        }
      >
        <form onSubmit={handleGenerate}>
          <div className="form-group">
            <label className="form-label">Key Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. CLI Deployment Token, Web Backend Key"
              value={keyName}
              onChange={(e) => setKeyName(e.target.value)}
              autoFocus
            />
          </div>

          <div className="form-group">
            <label className="form-label">Environment</label>
            <select
              className="form-select"
              value={keyEnv}
              onChange={(e) => setKeyEnv(e.target.value as CredentialEnvironment)}
            >
              <option value="DEVELOPMENT">DEVELOPMENT</option>
              <option value="STAGING">STAGING</option>
              <option value="PRODUCTION">PRODUCTION</option>
            </select>
          </div>
        </form>
      </Modal>

      {/* One-Time Revealed Key Modal */}
      <Modal
        isOpen={revealedKey !== null}
        onClose={() => setRevealedKey(null)}
        title="API Key Generated Successfully"
        footer={
          <Button variant="primary" onClick={() => setRevealedKey(null)}>
            I have saved this key
          </Button>
        }
      >
        {revealedKey && (
          <div>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
                padding: '10px 12px',
                background: 'rgba(245, 158, 11, 0.1)',
                border: '1px solid rgba(245, 158, 11, 0.3)',
                borderRadius: 'var(--radius-sm)',
                color: 'var(--amber-400)',
                fontSize: '12px',
                marginBottom: '16px',
              }}
            >
              <AlertTriangle size={16} />
              <span>
                Copy this API key now. For security purposes, it will never be displayed again.
              </span>
            </div>

            <div style={{ marginBottom: '16px' }}>
              <label className="form-label">API Key Secret</label>
              <CodeBlock code={revealedKey.apiKey} language="bash" title="API Key" />
            </div>

            <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
              Pass this key in your SDK configuration or as header <code>X-API-Key: {revealedKey.apiKey.substring(0, 10)}...</code>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};
