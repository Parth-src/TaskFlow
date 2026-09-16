import React, { useState } from 'react';
import { useProject } from '../context/ProjectContext';
import { Project } from '../types';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { Modal } from '../components/common/Modal';
import { EmptyState } from '../components/common/EmptyState';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import {
  Layers,
  Plus,
  CheckCircle2,
  FolderGit2,
  Calendar,
  KeyRound,
  GitBranch,
} from 'lucide-react';

interface ProjectsPageProps {
  onNavigate: (path: string) => void;
}

export const ProjectsPage: React.FC<ProjectsPageProps> = ({ onNavigate }) => {
  const { projects, activeProject, setActiveProject, createProject, loading, refreshProjects } =
    useProject();
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [creating, setCreating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    try {
      setCreating(true);
      setError(null);
      await createProject(name.trim());
      setName('');
      setCreateModalOpen(false);
    } catch (err: any) {
      setError(err?.message || 'Failed to create project in PostgreSQL');
    } finally {
      setCreating(false);
    }
  };

  if (loading && projects.length === 0) {
    return <LoadingSpinner message="Querying projects from PostgreSQL..." />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <Layers size={22} color="var(--cyan-400)" />
            <span>Project Isolation</span>
          </h1>
          <p className="page-subtitle">
            TaskFlow projects provide workspace boundaries for workflows, API credentials, and background executions.
          </p>
        </div>
        <Button
          variant="primary"
          icon={<Plus size={14} />}
          onClick={() => setCreateModalOpen(true)}
        >
          New Project
        </Button>
      </div>

      {projects.length === 0 ? (
        <EmptyState
          icon={Layers}
          title="No projects created yet"
          description="Create your first TaskFlow project to organize workflows and generate API credentials."
          action={
            <Button
              variant="primary"
              onClick={() => setCreateModalOpen(true)}
              icon={<Plus size={14} />}
            >
              Create Project
            </Button>
          }
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
          {projects.map((project) => {
            const isActive = activeProject?.id === project.id;
            return (
              <div
                key={project.id}
                className="card"
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  borderColor: isActive ? 'var(--cyan-500)' : 'var(--border-subtle)',
                  background: isActive ? 'rgba(6, 182, 212, 0.04)' : 'var(--bg-surface)',
                }}
              >
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <div
                        style={{
                          width: '32px',
                          height: '32px',
                          borderRadius: '6px',
                          background: isActive ? 'rgba(6, 182, 212, 0.15)' : 'var(--bg-surface-elevated)',
                          border: '1px solid ' + (isActive ? 'var(--cyan-500)' : 'var(--border-medium)'),
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          color: isActive ? 'var(--cyan-400)' : 'var(--text-secondary)',
                        }}
                      >
                        <Layers size={16} />
                      </div>
                      <div>
                        <h4 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>
                          {project.name}
                        </h4>
                        <span className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                          ID: {project.id}
                        </span>
                      </div>
                    </div>

                    {isActive && (
                      <span className="badge badge-completed">
                        <CheckCircle2 size={11} /> ACTIVE
                      </span>
                    )}
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', margin: '16px 0', fontSize: '12px', color: 'var(--text-secondary)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <FolderGit2 size={13} color="var(--text-muted)" />
                      <span>Repository: </span>
                      <strong style={{ color: project.githubRepository ? 'var(--cyan-400)' : 'var(--text-muted)' }}>
                        {project.githubRepository || 'None connected'}
                      </strong>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Calendar size={13} color="var(--text-muted)" />
                      <span>Created: {new Date(project.createdAt).toLocaleDateString()}</span>
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '8px', paddingTop: '14px', borderTop: '1px solid var(--border-subtle)' }}>
                  {!isActive && (
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => setActiveProject(project)}
                    >
                      Set Active
                    </Button>
                  )}
                  <Button
                    variant="ghost"
                    size="sm"
                    icon={<KeyRound size={12} />}
                    onClick={() => {
                      setActiveProject(project);
                      onNavigate('/dashboard/credentials');
                    }}
                  >
                    API Keys
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    icon={<GitBranch size={12} />}
                    onClick={() => {
                      setActiveProject(project);
                      onNavigate('/dashboard/workflows');
                    }}
                  >
                    Workflows
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create Modal */}
      <Modal
        isOpen={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        title="Create New Project"
        footer={
          <div style={{ display: 'flex', gap: '8px' }}>
            <Button variant="secondary" onClick={() => setCreateModalOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="primary"
              loading={creating}
              onClick={handleCreate}
              disabled={!name.trim()}
            >
              Create Project
            </Button>
          </div>
        }
      >
        <form onSubmit={handleCreate}>
          <div className="form-group">
            <label className="form-label">Project Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Core Payment Platform, Ingestion Cluster"
              value={name}
              onChange={(e) => setName(e.target.value)}
              autoFocus
            />
          </div>
          {error && (
            <div style={{ color: 'var(--rose-400)', fontSize: '12px', marginTop: '8px' }}>
              {error}
            </div>
          )}
        </form>
      </Modal>
    </div>
  );
};
