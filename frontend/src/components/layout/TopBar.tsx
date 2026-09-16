import React, { useState } from 'react';
import { useProject } from '../../context/ProjectContext';
import { useAuth } from '../../context/AuthContext';
import { ChevronDown, Plus, Play, Layers, Github } from 'lucide-react';
import { Button } from '../common/Button';
import { Modal } from '../common/Modal';

interface TopBarProps {
  onQuickRun?: () => void;
}

export const TopBar: React.FC<TopBarProps> = ({ onQuickRun }) => {
  const { user } = useAuth();
  const { projects, activeProject, setActiveProject, createProject } = useProject();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [newProjectName, setNewProjectName] = useState('');
  const [creating, setCreating] = useState(false);

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProjectName.trim()) return;

    try {
      setCreating(true);
      await createProject(newProjectName.trim());
      setNewProjectName('');
      setCreateModalOpen(false);
      setDropdownOpen(false);
    } catch (err) {
      console.error('Failed to create project:', err);
    } finally {
      setCreating(false);
    }
  };

  return (
    <>
      <header className="topbar">
        {/* Left: Project Selector */}
        <div className="topbar-left">
          <div style={{ position: 'relative' }}>
            <button
              className="project-selector"
              onClick={() => setDropdownOpen(!dropdownOpen)}
            >
              <Layers size={14} color="var(--cyan-400)" />
              <span>{activeProject ? activeProject.name : 'Select Project'}</span>
              <ChevronDown size={14} color="var(--text-muted)" />
            </button>

            {dropdownOpen && (
              <>
                <div
                  style={{ position: 'fixed', inset: 0, zIndex: 45 }}
                  onClick={() => setDropdownOpen(false)}
                />
                <div
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 6px)',
                    left: 0,
                    width: '240px',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-medium)',
                    borderRadius: 'var(--radius-md)',
                    boxShadow: 'var(--shadow-lg)',
                    padding: '6px',
                    zIndex: 50,
                  }}
                >
                  <div
                    style={{
                      fontSize: '11px',
                      color: 'var(--text-muted)',
                      padding: '6px 8px',
                      fontWeight: 600,
                      textTransform: 'uppercase',
                    }}
                  >
                    Your Projects
                  </div>

                  <div style={{ maxHeight: '180px', overflowY: 'auto' }}>
                    {projects.map((proj) => (
                      <button
                        key={proj.id}
                        onClick={() => {
                          setActiveProject(proj);
                          setDropdownOpen(false);
                        }}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          width: '100%',
                          padding: '8px 10px',
                          borderRadius: 'var(--radius-sm)',
                          background:
                            activeProject?.id === proj.id
                              ? 'rgba(6, 182, 212, 0.1)'
                              : 'transparent',
                          color:
                            activeProject?.id === proj.id
                              ? 'var(--cyan-400)'
                              : 'var(--text-primary)',
                          fontSize: '13px',
                          textAlign: 'left',
                        }}
                      >
                        <span style={{ fontWeight: activeProject?.id === proj.id ? 600 : 400 }}>
                          {proj.name}
                        </span>
                        {activeProject?.id === proj.id && (
                          <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--cyan-400)' }} />
                        )}
                      </button>
                    ))}
                  </div>

                  <div style={{ borderTop: '1px solid var(--border-subtle)', marginTop: '4px', paddingTop: '4px' }}>
                    <button
                      onClick={() => {
                        setDropdownOpen(false);
                        setCreateModalOpen(true);
                      }}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        width: '100%',
                        padding: '8px 10px',
                        borderRadius: 'var(--radius-sm)',
                        background: 'transparent',
                        color: 'var(--text-secondary)',
                        fontSize: '12px',
                        fontWeight: 500,
                      }}
                    >
                      <Plus size={14} />
                      <span>Create New Project</span>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>

        {/* Right: Engine Telemetry & Quick Action */}
        <div className="topbar-right">
          <div className="system-status-indicator">
            <span className="status-dot pulse" />
            <span>ENGINE ACTIVE</span>
          </div>

          {onQuickRun && (
            <Button
              variant="primary"
              size="sm"
              icon={<Play size={13} />}
              onClick={onQuickRun}
            >
              Run Workflow
            </Button>
          )}

          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '4px 10px',
              background: 'var(--bg-surface-elevated)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-sm)',
              fontSize: '12px',
              color: 'var(--text-secondary)',
            }}
          >
            <Github size={13} color="var(--text-muted)" />
            <span className="font-mono">{user?.username || 'user'}</span>
          </div>
        </div>
      </header>

      {/* Create Project Modal */}
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
              onClick={handleCreateProject}
              disabled={!newProjectName.trim()}
            >
              Create Project
            </Button>
          </div>
        }
      >
        <form onSubmit={handleCreateProject}>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
            TaskFlow scopes workflows, credentials, execution records, and Dead Letter Queue events to distinct projects.
          </p>
          <div className="form-group">
            <label className="form-label">Project Name</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Production Billing, Core Services"
              value={newProjectName}
              onChange={(e) => setNewProjectName(e.target.value)}
              autoFocus
            />
          </div>
        </form>
      </Modal>
    </>
  );
};
