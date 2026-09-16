import React, { useState } from 'react';
import { WorkflowSummary } from '../../types';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';
import { useProject } from '../../context/ProjectContext';
import { workflowsApi, TriggerExecutionResponse } from '../../api/workflows';
import { Play, CheckCircle2, ArrowRight, Server } from 'lucide-react';

interface TriggerWorkflowModalProps {
  isOpen: boolean;
  onClose: () => void;
  workflow: WorkflowSummary | null;
  onExecutionStarted?: (executionId: string) => void;
}

export const TriggerWorkflowModal: React.FC<TriggerWorkflowModalProps> = ({
  isOpen,
  onClose,
  workflow,
  onExecutionStarted,
}) => {
  const { activeProject } = useProject();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<TriggerExecutionResponse | null>(null);

  if (!workflow) return null;

  const handleTrigger = async () => {
    if (!activeProject) {
      setError('Please select or create an active project first.');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const res = await workflowsApi.executeWorkflow(workflow.id, activeProject.id);
      setResult(res);
      onExecutionStarted?.(res.executionId);
    } catch (e: any) {
      setError(e?.message || 'Failed to trigger workflow execution');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setResult(null);
    setError(null);
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={result ? 'Workflow Execution Started' : `Run Workflow: ${workflow.name}`}
      footer={
        result ? (
          <Button
            variant="primary"
            onClick={() => {
              handleClose();
              window.location.hash = `#/dashboard/executions/${result.executionId}`;
            }}
            icon={<ArrowRight size={14} />}
          >
            Open Execution Detail
          </Button>
        ) : (
          <div style={{ display: 'flex', gap: '8px' }}>
            <Button variant="secondary" onClick={handleClose}>
              Cancel
            </Button>
            <Button
              variant="primary"
              loading={loading}
              onClick={handleTrigger}
              icon={<Play size={14} />}
            >
              Start Execution
            </Button>
          </div>
        )
      }
    >
      {result ? (
        <div style={{ textAlign: 'center', padding: '12px 0' }}>
          <div
            style={{
              width: '48px',
              height: '48px',
              borderRadius: '50%',
              backgroundColor: 'rgba(16, 185, 129, 0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--emerald-400)',
              margin: '0 auto 16px auto',
            }}
          >
            <CheckCircle2 size={24} />
          </div>
          <h4 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '6px' }}>
            Asynchronous Execution Dispatched
          </h4>
          <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
            TaskFlow accepted the workflow submission and has begun dependency resolution in the background.
          </p>

          <div
            style={{
              background: '#05080e',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-sm)',
              padding: '12px',
              textAlign: 'left',
              fontSize: '12px',
              fontFamily: 'var(--font-mono)',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
              <span style={{ color: 'var(--text-muted)' }}>Execution ID:</span>
              <span style={{ color: 'var(--cyan-400)', fontWeight: 600 }}>{result.executionId}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
              <span style={{ color: 'var(--text-muted)' }}>Initial Status:</span>
              <span style={{ color: 'var(--emerald-400)' }}>{result.status}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Project:</span>
              <span style={{ color: 'var(--text-secondary)' }}>{activeProject?.name}</span>
            </div>
          </div>
        </div>
      ) : (
        <div>
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '16px' }}>
            Executing this workflow will submit <strong>{workflow.taskCount} tasks</strong> to TaskFlow's background orchestration engine.
          </p>

          <div style={{ marginBottom: '16px' }}>
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
              {activeProject ? `${activeProject.name} (${activeProject.id.substring(0, 8)}...)` : 'No project selected'}
            </div>
          </div>

          <div>
            <label className="form-label">Tasks to be dispatched</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {workflow.tasks.map((task) => (
                <div
                  key={task.id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '8px 12px',
                    background: 'var(--bg-surface-elevated)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-sm)',
                    fontSize: '12px',
                  }}
                >
                  <span style={{ fontFamily: 'var(--font-mono)', color: 'var(--text-primary)', fontWeight: 600 }}>
                    {task.id}
                  </span>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)', fontSize: '11px' }}>
                    <Server size={11} />
                    <span>{task.worker}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {error && (
            <div
              style={{
                marginTop: '16px',
                padding: '10px 12px',
                background: 'rgba(244, 63, 94, 0.1)',
                border: '1px solid rgba(244, 63, 94, 0.3)',
                borderRadius: 'var(--radius-sm)',
                color: 'var(--rose-400)',
                fontSize: '12px',
              }}
            >
              {error}
            </div>
          )}
        </div>
      )}
    </Modal>
  );
};
