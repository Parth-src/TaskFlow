import React, { useEffect, useState, useCallback } from 'react';
import { useProject } from '../context/ProjectContext';
import { executionsApi } from '../api/executions';
import { workflowsApi } from '../api/workflows';
import { TaskExecution, WorkflowSummary } from '../types';
import { ExecutionStatusBadge } from '../components/execution/ExecutionStatusBadge';
import { TaskExecutionTable } from '../components/execution/TaskExecutionTable';
import { ExecutionTimeline } from '../components/execution/ExecutionTimeline';
import { WorkflowGraph } from '../components/workflow/WorkflowGraph';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import {
  ArrowLeft,
  RotateCcw,
  Clock,
  CheckCircle2,
  XCircle,
  Activity,
  Layers,
  GitBranch,
} from 'lucide-react';

interface ExecutionDetailPageProps {
  executionId: string;
  onNavigate: (path: string) => void;
}

export const ExecutionDetailPage: React.FC<ExecutionDetailPageProps> = ({
  executionId,
  onNavigate,
}) => {
  const { activeProject } = useProject();
  const [tasks, setTasks] = useState<TaskExecution[]>([]);
  const [workflow, setWorkflow] = useState<WorkflowSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadExecutionData = useCallback(async () => {
    try {
      setError(null);
      const data = await executionsApi.getExecution(executionId, activeProject?.id);
      setTasks(data || []);

      if (data && data.length > 0 && !workflow) {
        const wfId = data[0].workflowId;
        try {
          const wf = await workflowsApi.getWorkflow(wfId);
          setWorkflow(wf);
        } catch {
          // If workflow def can't be fetched, construct fallback from tasks
        }
      }
    } catch (err: any) {
      console.error('Failed to load execution detail:', err);
      setError(err?.message || 'Failed to load execution details');
    } finally {
      setLoading(false);
    }
  }, [executionId, activeProject, workflow]);

  useEffect(() => {
    loadExecutionData();
  }, [loadExecutionData]);

  // Auto-refresh while tasks are RUNNING or PENDING
  useEffect(() => {
    const hasActive = tasks.some(
      (t) => t.status === 'RUNNING' || t.status === 'PENDING' || t.status === 'READY'
    );
    if (!hasActive && tasks.length > 0) return;

    const interval = setInterval(loadExecutionData, 3000);
    return () => clearInterval(interval);
  }, [tasks, loadExecutionData]);

  if (loading && tasks.length === 0) {
    return <LoadingSpinner message="Fetching live execution state from Redis..." />;
  }

  if (error && tasks.length === 0) {
    return <ErrorState message={error} onRetry={loadExecutionData} />;
  }

  // Derive overall status
  const isRunning = tasks.some((t) => t.status === 'RUNNING');
  const hasFailed = tasks.some((t) => t.status === 'FAILED');
  const isAllCompleted = tasks.length > 0 && tasks.every((t) => t.status === 'COMPLETED');
  const overallStatus = isRunning
    ? 'RUNNING'
    : hasFailed
    ? 'FAILED'
    : isAllCompleted
    ? 'COMPLETED'
    : 'PENDING';

  // Calculate total execution duration
  let earliestStart: number | null = null;
  let latestEnd: number | null = null;
  tasks.forEach((t) => {
    if (t.startedAt) {
      const s = new Date(t.startedAt).getTime();
      if (earliestStart === null || s < earliestStart) earliestStart = s;
    }
    if (t.completedAt) {
      const e = new Date(t.completedAt).getTime();
      if (latestEnd === null || e > latestEnd) latestEnd = e;
    }
  });

  const totalDurationMs =
    earliestStart && latestEnd ? latestEnd - earliestStart : null;

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <button
            onClick={() => onNavigate('/dashboard/executions')}
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              background: 'transparent',
              color: 'var(--cyan-400)',
              fontSize: '12px',
              fontWeight: 500,
              marginBottom: '8px',
            }}
          >
            <ArrowLeft size={13} /> Back to Executions
          </button>
          <h1 className="page-title">
            <span className="font-mono" style={{ fontSize: '20px' }}>
              Execution {executionId.substring(0, 16)}...
            </span>
            <ExecutionStatusBadge status={overallStatus} />
          </h1>
          <p className="page-subtitle">
            Workflow:{' '}
            <strong style={{ color: 'var(--text-primary)' }}>
              {tasks.length > 0 ? tasks[0].workflowId : 'Unknown'}
            </strong>
          </p>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <Button
            variant="secondary"
            size="sm"
            icon={<RotateCcw size={13} />}
            onClick={loadExecutionData}
          >
            Refresh Live State
          </Button>
        </div>
      </div>

      {/* Execution Telemetry Summary Cards */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '16px',
          marginBottom: '24px',
        }}
      >
        <div className="card" style={{ padding: '16px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>EXECUTION STATUS</div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ExecutionStatusBadge status={overallStatus} />
          </div>
        </div>

        <div className="card" style={{ padding: '16px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>TOTAL DURATION</div>
          <div className="font-mono" style={{ fontSize: '16px', fontWeight: 600, color: 'var(--text-primary)' }}>
            {totalDurationMs != null ? `${totalDurationMs} ms` : isRunning ? 'In Progress...' : '—'}
          </div>
        </div>

        <div className="card" style={{ padding: '16px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>TASKS RESOLVED</div>
          <div className="font-mono" style={{ fontSize: '16px', fontWeight: 600, color: 'var(--emerald-400)' }}>
            {tasks.filter((t) => t.status === 'COMPLETED').length} / {tasks.length}
          </div>
        </div>

        <div className="card" style={{ padding: '16px' }}>
          <div style={{ fontSize: '11px', color: 'var(--text-muted)', marginBottom: '4px' }}>STARTED AT</div>
          <div className="font-mono" style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
            {earliestStart ? new Date(earliestStart).toLocaleTimeString() : '—'}
          </div>
        </div>
      </div>

      {/* DAG Visualizer with Execution States */}
      {workflow && (
        <div style={{ marginBottom: '24px' }}>
          <Card
            title={
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <GitBranch size={16} color="var(--cyan-400)" />
                <span>Live DAG Execution Flow</span>
              </div>
            }
            subtitle="Node colors represent real-time task state transitions in Redis."
          >
            <WorkflowGraph
              tasks={workflow.tasks}
              taskExecutions={tasks}
            />
          </Card>
        </div>
      )}

      {/* Execution Waterfall Timeline */}
      <div style={{ marginBottom: '24px' }}>
        <Card
          title={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Clock size={16} color="var(--cyan-400)" />
              <span>Execution Lifecycle Waterfall</span>
            </div>
          }
          subtitle="Shows sequential dependencies and parallel task concurrency intervals."
        >
          <ExecutionTimeline tasks={tasks} />
        </Card>
      </div>

      {/* Detailed Tasks Matrix */}
      <div>
        <Card
          title={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={16} color="var(--violet-400)" />
              <span>Task Execution State Matrix</span>
            </div>
          }
          subtitle="Individual task attempt counts, worker assignments, timing, and error logs."
        >
          <TaskExecutionTable tasks={tasks} />
        </Card>
      </div>
    </div>
  );
};
