import React, { useEffect, useState, useCallback } from 'react';
import { useProject } from '../context/ProjectContext';
import { repositoriesApi } from '../api/repositories';
import { projectsApi } from '../api/projects';
import { ConnectedRepository, GitHubRepository } from '../types';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Modal } from '../components/common/Modal';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { EmptyState } from '../components/common/EmptyState';
import {
  FolderGit2,
  Github,
  CheckCircle2,
  RotateCcw,
  Link,
  Lock,
  GitBranch,
} from 'lucide-react';

export const RepositoriesPage: React.FC = () => {
  const { activeProject, refreshProjects } = useProject();
  const [connectedRepos, setConnectedRepos] = useState<ConnectedRepository[]>([]);
  const [githubRepos, setGithubRepos] = useState<GitHubRepository[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [connectModalOpen, setConnectModalOpen] = useState(false);
  const [selectedGhRepo, setSelectedGhRepo] = useState<string>('');
  const [connecting, setConnecting] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const loadRepositories = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const [connected, github] = await Promise.all([
        repositoriesApi.getConnectedRepositories(),
        repositoriesApi.getGitHubRepositories(),
      ]);
      setConnectedRepos(connected || []);
      setGithubRepos(github || []);
    } catch (err: any) {
      console.error('Failed to load repositories:', err);
      setError(err?.message || 'Failed to query repositories');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRepositories();
  }, [loadRepositories]);

  const handleConnect = async () => {
    if (!activeProject || !selectedGhRepo) return;

    try {
      setConnecting(true);
      setError(null);
      await projectsApi.connectRepository(activeProject.id, selectedGhRepo);
      setSuccessMessage(`Repository '${selectedGhRepo}' connected to project '${activeProject.name}'`);
      setTimeout(() => setSuccessMessage(null), 4000);
      setConnectModalOpen(false);
      await refreshProjects();
      await loadRepositories();
    } catch (err: any) {
      setError(err?.message || 'Failed to connect repository to project');
    } finally {
      setConnecting(false);
    }
  };

  if (loading && connectedRepos.length === 0 && githubRepos.length === 0) {
    return <LoadingSpinner message="Querying GitHub and project repositories..." />;
  }

  if (error && connectedRepos.length === 0 && githubRepos.length === 0) {
    return <ErrorState message={error} onRetry={loadRepositories} />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <FolderGit2 size={22} color="var(--cyan-400)" />
            <span>Repository Connections</span>
          </h1>
          <p className="page-subtitle">
            Synchronize workflow definitions and background worker implementations with GitHub repositories.
          </p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          icon={<RotateCcw size={13} />}
          onClick={loadRepositories}
        >
          Refresh
        </Button>
      </div>

      {successMessage && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '10px 14px',
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            borderRadius: 'var(--radius-sm)',
            color: 'var(--emerald-400)',
            fontSize: '13px',
            marginBottom: '20px',
          }}
        >
          <CheckCircle2 size={16} />
          <span>{successMessage}</span>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        {/* Connected TaskFlow Repositories */}
        <Card
          title={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <FolderGit2 size={16} color="var(--emerald-400)" />
              <span>Connected TaskFlow Repositories ({connectedRepos.length})</span>
            </div>
          }
          subtitle="Repositories actively linked to TaskFlow projects."
        >
          {connectedRepos.length === 0 ? (
            <div style={{ padding: '24px 0', textAlign: 'center', color: 'var(--text-muted)', fontSize: '13px' }}>
              No repositories connected yet. Link a GitHub repository from the list on the right.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {connectedRepos.map((repo) => (
                <div
                  key={repo.projectId}
                  style={{
                    padding: '12px 14px',
                    background: 'var(--bg-surface-elevated)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-sm)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Github size={14} color="var(--text-primary)" />
                      <strong className="font-mono" style={{ color: 'var(--cyan-400)', fontSize: '13px' }}>
                        {repo.repository}
                      </strong>
                    </div>
                    <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px' }}>
                      Project: <strong style={{ color: 'var(--text-secondary)' }}>{repo.projectName}</strong>
                    </div>
                  </div>
                  <span className="badge badge-completed">
                    <CheckCircle2 size={11} /> LINKED
                  </span>
                </div>
              ))}
            </div>
          )}
        </Card>

        {/* GitHub Account Repositories */}
        <Card
          title={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Github size={16} color="var(--text-primary)" />
              <span>GitHub Account Repositories ({githubRepos.length})</span>
            </div>
          }
          subtitle="Repositories discovered from your authenticated GitHub account."
        >
          {githubRepos.length === 0 ? (
            <div style={{ padding: '24px 0', textAlign: 'center', color: 'var(--text-muted)', fontSize: '13px' }}>
              No GitHub repositories found. Connect via GitHub OAuth with repository permissions.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {githubRepos.map((gh) => {
                const isConnected = connectedRepos.some((c) => c.repository === gh.fullName);
                return (
                  <div
                    key={gh.fullName}
                    style={{
                      padding: '12px 14px',
                      background: 'var(--bg-surface-elevated)',
                      border: '1px solid var(--border-subtle)',
                      borderRadius: 'var(--radius-sm)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <span className="font-mono" style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
                          {gh.fullName}
                        </span>
                        {gh.isPrivate && <Lock size={12} color="var(--amber-400)" />}
                      </div>
                      <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '2px' }}>
                        {gh.description}
                      </div>
                    </div>

                    {isConnected ? (
                      <span className="badge badge-completed" style={{ fontSize: '10px' }}>
                        CONNECTED
                      </span>
                    ) : (
                      <Button
                        variant="secondary"
                        size="sm"
                        icon={<Link size={12} />}
                        onClick={() => {
                          setSelectedGhRepo(gh.fullName);
                          setConnectModalOpen(true);
                        }}
                      >
                        Connect
                      </Button>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </Card>
      </div>

      {/* Connect Repository Modal */}
      <Modal
        isOpen={connectModalOpen}
        onClose={() => setConnectModalOpen(false)}
        title="Connect Repository to Project"
        footer={
          <div style={{ display: 'flex', gap: '8px' }}>
            <Button variant="secondary" onClick={() => setConnectModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              loading={connecting}
              onClick={handleConnect}
              disabled={!activeProject}
            >
              Confirm Connection
            </Button>
          </div>
        }
      >
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '14px' }}>
          Connect repository <strong>{selectedGhRepo}</strong> to your active project:
        </p>

        <div style={{ marginBottom: '14px' }}>
          <label className="form-label">Active Project</label>
          <div
            style={{
              padding: '8px 12px',
              background: 'var(--bg-app)',
              border: '1px solid var(--border-medium)',
              borderRadius: 'var(--radius-sm)',
              fontSize: '13px',
              color: 'var(--text-primary)',
              fontFamily: 'var(--font-mono)',
            }}
          >
            {activeProject ? activeProject.name : 'No active project selected'}
          </div>
        </div>
      </Modal>
    </div>
  );
};
