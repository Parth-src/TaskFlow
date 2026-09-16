import React, { useEffect, useState, useCallback } from 'react';
import { useProject } from '../context/ProjectContext';
import { templatesApi } from '../api/templates';
import { workflowsApi } from '../api/workflows';
import { Template } from '../types';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { LayoutTemplate, Play, ArrowRight, Server, CheckCircle2, GitBranch } from 'lucide-react';

interface TemplatesPageProps {
  onNavigate: (path: string) => void;
}

export const TemplatesPage: React.FC<TemplatesPageProps> = ({ onNavigate }) => {
  const { activeProject } = useProject();
  const [templates, setTemplates] = useState<Template[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deployingId, setDeployingId] = useState<string | null>(null);
  const [deployedSuccess, setDeployedSuccess] = useState<string | null>(null);

  const loadTemplates = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await templatesApi.getTemplates();
      setTemplates(data || []);
    } catch (err: any) {
      console.error('Failed to load templates:', err);
      setError(err?.message || 'Failed to load templates');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTemplates();
  }, [loadTemplates]);

  const handleUseTemplate = async (template: Template) => {
    if (!activeProject) {
      setError('Please select an active project first.');
      return;
    }

    try {
      setDeployingId(template.id);
      setError(null);
      // Map template to workflow ID
      let targetWorkflowId = 'payment-flow';
      if (template.id.includes('order')) targetWorkflowId = 'order-processing';
      if (template.id.includes('notification')) targetWorkflowId = 'notification-pipeline';
      if (template.id.includes('data')) targetWorkflowId = 'data-processing';

      const res = await workflowsApi.executeWorkflow(targetWorkflowId, activeProject.id);
      setDeployedSuccess(`Template '${template.name}' dispatched to execution '${res.executionId}'!`);
      setTimeout(() => {
        onNavigate(`/dashboard/executions/${res.executionId}`);
      }, 1200);
    } catch (err: any) {
      setError(err?.message || 'Failed to dispatch workflow template');
    } finally {
      setDeployingId(null);
    }
  };

  if (loading && templates.length === 0) {
    return <LoadingSpinner message="Querying production workflow blueprints..." />;
  }

  if (error && templates.length === 0) {
    return <ErrorState message={error} onRetry={loadTemplates} />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <LayoutTemplate size={22} color="var(--violet-400)" />
            <span>Workflow Templates</span>
          </h1>
          <p className="page-subtitle">
            Battle-tested architecture blueprints ready for instant background execution in your active project.
          </p>
        </div>
      </div>

      {deployedSuccess && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '12px 16px',
            background: 'rgba(16, 185, 129, 0.1)',
            border: '1px solid rgba(16, 185, 129, 0.3)',
            borderRadius: 'var(--radius-sm)',
            color: 'var(--emerald-400)',
            fontSize: '13px',
            marginBottom: '20px',
          }}
        >
          <CheckCircle2 size={16} />
          <span>{deployedSuccess}</span>
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '20px' }}>
        {templates.map((template) => (
          <div
            key={template.id}
            className="card"
            style={{
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
            }}
          >
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <span
                  style={{
                    fontSize: '10px',
                    fontFamily: 'var(--font-mono)',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    background: 'rgba(139, 92, 246, 0.15)',
                    color: 'var(--violet-400)',
                    fontWeight: 600,
                    textTransform: 'uppercase',
                  }}
                >
                  {template.category}
                </span>
                <span className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                  {template.taskCount} tasks
                </span>
              </div>

              <h3 style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '8px' }}>
                {template.name}
              </h3>

              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '16px' }}>
                {template.description}
              </p>

              {/* Task pipeline preview */}
              <div style={{ marginBottom: '20px' }}>
                <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '8px', fontWeight: 600, textTransform: 'uppercase' }}>
                  Execution Pipeline
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  {template.tasks.map((task, idx) => (
                    <div
                      key={task.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '6px 10px',
                        background: 'var(--bg-surface-elevated)',
                        border: '1px solid var(--border-subtle)',
                        borderRadius: '4px',
                        fontSize: '11px',
                        fontFamily: 'var(--font-mono)',
                      }}
                    >
                      <span style={{ color: 'var(--text-primary)' }}>
                        {idx + 1}. {task.id}
                      </span>
                      <span style={{ color: 'var(--cyan-400)' }}>
                        worker: {task.worker}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div style={{ paddingTop: '16px', borderTop: '1px solid var(--border-subtle)' }}>
              <Button
                variant="primary"
                style={{ width: '100%' }}
                icon={<Play size={13} />}
                loading={deployingId === template.id}
                onClick={() => handleUseTemplate(template)}
              >
                Use Template in {activeProject?.name || 'Active Project'}
              </Button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
