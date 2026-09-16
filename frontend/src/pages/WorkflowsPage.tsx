import React, { useEffect, useState, useCallback } from 'react';
import { workflowsApi } from '../api/workflows';
import { WorkflowSummary, WorkflowTask } from '../types';
import { WorkflowCard } from '../components/workflow/WorkflowCard';
import { WorkflowGraph } from '../components/workflow/WorkflowGraph';
import { TriggerWorkflowModal } from '../components/workflow/TriggerWorkflowModal';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { EmptyState } from '../components/common/EmptyState';
import { GitBranch, Play, Server, ArrowLeft, Layers } from 'lucide-react';

interface WorkflowsPageProps {
  onNavigate: (path: string) => void;
  workflowIdParam?: string;
}

export const WorkflowsPage: React.FC<WorkflowsPageProps> = ({
  onNavigate,
  workflowIdParam,
}) => {
  const [workflows, setWorkflows] = useState<WorkflowSummary[]>([]);
  const [selectedWorkflow, setSelectedWorkflow] = useState<WorkflowSummary | null>(null);
  const [selectedTask, setSelectedTask] = useState<WorkflowTask | null>(null);
  const [triggerModalOpen, setTriggerModalOpen] = useState(false);
  const [triggerTargetWorkflow, setTriggerTargetWorkflow] = useState<WorkflowSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadWorkflows = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const list = await workflowsApi.getWorkflows();
      setWorkflows(list || []);

      if (workflowIdParam && list) {
        const found = list.find((w) => w.id === workflowIdParam || w.name === workflowIdParam);
        if (found) {
          setSelectedWorkflow(found);
          if (found.tasks.length > 0) setSelectedTask(found.tasks[0]);
          return;
        }
      }

      if (list && list.length > 0 && !selectedWorkflow) {
        setSelectedWorkflow(list[0]);
        if (list[0].tasks.length > 0) setSelectedTask(list[0].tasks[0]);
      }
    } catch (err: any) {
      console.error('Failed to load workflows:', err);
      setError(err?.message || 'Failed to load workflows');
    } finally {
      setLoading(false);
    }
  }, [workflowIdParam]);

  useEffect(() => {
    loadWorkflows();
  }, [loadWorkflows]);

  const handleSelectWorkflow = (wf: WorkflowSummary) => {
    setSelectedWorkflow(wf);
    if (wf.tasks.length > 0) {
      setSelectedTask(wf.tasks[0]);
    } else {
      setSelectedTask(null);
    }
  };

  const handleOpenTrigger = (wf: WorkflowSummary) => {
    setTriggerTargetWorkflow(wf);
    setTriggerModalOpen(true);
  };

  if (loading && workflows.length === 0) {
    return <LoadingSpinner message="Loading workflow definitions & DAG dependencies..." />;
  }

  if (error && workflows.length === 0) {
    return <ErrorState message={error} onRetry={loadWorkflows} />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <GitBranch size={22} color="var(--cyan-400)" />
            <span>Workflows</span>
          </h1>
          <p className="page-subtitle">
            Defined business processes with topological dependency resolution and language-free worker dispatch.
          </p>
        </div>
        {selectedWorkflow && (
          <Button
            variant="primary"
            icon={<Play size={14} />}
            onClick={() => handleOpenTrigger(selectedWorkflow)}
          >
            Run {selectedWorkflow.name}
          </Button>
        )}
      </div>

      {workflows.length === 0 ? (
        <EmptyState
          icon={GitBranch}
          title="No workflows configured"
          description="Create a workflow definition YAML in your repository or choose from pre-built templates."
          action={
            <Button
              variant="primary"
              onClick={() => onNavigate('/dashboard/templates')}
            >
              Browse Templates
            </Button>
          }
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '20px' }}>
          {/* Workflows List Column */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div style={{ fontSize: '11px', fontWeight: 700, textTransform: 'uppercase', color: 'var(--text-muted)', letterSpacing: '0.05em' }}>
              Available Workflows ({workflows.length})
            </div>
            {workflows.map((wf) => {
              const isSelected = selectedWorkflow?.id === wf.id;
              return (
                <div
                  key={wf.id}
                  className="card"
                  style={{
                    padding: '14px 16px',
                    cursor: 'pointer',
                    borderColor: isSelected ? 'var(--cyan-500)' : 'var(--border-subtle)',
                    background: isSelected ? 'rgba(6, 182, 212, 0.05)' : 'var(--bg-surface)',
                  }}
                  onClick={() => handleSelectWorkflow(wf)}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                    <h4 style={{ fontSize: '14px', fontWeight: 600, color: isSelected ? 'var(--cyan-400)' : 'var(--text-primary)' }}>
                      {wf.name}
                    </h4>
                    <span style={{ fontSize: '11px', fontFamily: 'var(--font-mono)', color: 'var(--text-muted)' }}>
                      {wf.taskCount} tasks
                    </span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '10px' }}>
                    <span style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                      DAG Blueprint
                    </span>
                    <Button
                      variant="ghost"
                      size="sm"
                      icon={<Play size={11} />}
                      onClick={(e) => {
                        e.stopPropagation();
                        handleOpenTrigger(wf);
                      }}
                    >
                      Run
                    </Button>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Workflow Detail & DAG Graph */}
          {selectedWorkflow && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              <Card
                title={
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <GitBranch size={16} color="var(--cyan-400)" />
                    <span>DAG Graph: {selectedWorkflow.name}</span>
                  </div>
                }
                subtitle="Interactive dependency graph. Click on any task node to inspect worker details."
                action={
                  <Button
                    variant="primary"
                    size="sm"
                    icon={<Play size={13} />}
                    onClick={() => handleOpenTrigger(selectedWorkflow)}
                  >
                    Execute Workflow
                  </Button>
                }
              >
                <WorkflowGraph
                  tasks={selectedWorkflow.tasks}
                  onSelectTask={(task) => setSelectedTask(task)}
                  selectedTaskId={selectedTask?.id}
                />
              </Card>

              {/* Task Detail Inspector */}
              {selectedTask && (
                <Card
                  title={
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Server size={15} color="var(--violet-400)" />
                      <span>Task Inspector: {selectedTask.id}</span>
                    </div>
                  }
                >
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
                    <div style={{ background: '#05080e', padding: '12px', borderRadius: '4px', border: '1px solid var(--border-subtle)' }}>
                      <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>TASK ID</div>
                      <div className="font-mono" style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>
                        {selectedTask.id}
                      </div>
                    </div>

                    <div style={{ background: '#05080e', padding: '12px', borderRadius: '4px', border: '1px solid var(--border-subtle)' }}>
                      <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>TARGET WORKER</div>
                      <div className="font-mono" style={{ fontSize: '13px', fontWeight: 600, color: 'var(--cyan-400)' }}>
                        {selectedTask.worker}
                      </div>
                    </div>

                    <div style={{ background: '#05080e', padding: '12px', borderRadius: '4px', border: '1px solid var(--border-subtle)' }}>
                      <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>PREREQUISITE DEPENDENCIES</div>
                      <div className="font-mono" style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        {selectedTask.dependsOn.length > 0 ? selectedTask.dependsOn.join(', ') : 'None (Ready immediately)'}
                      </div>
                    </div>
                  </div>
                </Card>
              )}
            </div>
          )}
        </div>
      )}

      {/* Trigger Modal */}
      <TriggerWorkflowModal
        isOpen={triggerModalOpen}
        onClose={() => setTriggerModalOpen(false)}
        workflow={triggerTargetWorkflow}
        onExecutionStarted={(executionId) => {
          onNavigate(`/dashboard/executions/${executionId}`);
        }}
      />
    </div>
  );
};
