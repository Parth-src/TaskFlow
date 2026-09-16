import React, { useEffect, useState, useCallback } from 'react';
import { useProject } from '../context/ProjectContext';
import { overviewApi } from '../api/overview';
import { executionsApi } from '../api/executions';
import { workersApi } from '../api/workers';
import { OverviewMetrics, ExecutionSummary, WorkerMetadata } from '../types';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { EmptyState } from '../components/common/EmptyState';
import { ExecutionStatusBadge } from '../components/execution/ExecutionStatusBadge';
import {
  Layers,
  GitBranch,
  Play,
  CheckCircle2,
  XCircle,
  Clock,
  Server,
  AlertOctagon,
  ArrowRight,
  RotateCcw,
  Activity,
  AlertTriangle,
} from 'lucide-react';

interface DashboardOverviewProps {
  onNavigate: (path: string) => void;
}

export const DashboardOverview: React.FC<DashboardOverviewProps> = ({ onNavigate }) => {
  const { activeProject } = useProject();
  const [metrics, setMetrics] = useState<OverviewMetrics | null>(null);
  const [recentExecutions, setRecentExecutions] = useState<ExecutionSummary[]>([]);
  const [workers, setWorkers] = useState<WorkerMetadata[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const [overviewData, executionsData, workersData] = await Promise.all([
        overviewApi.getOverview(activeProject?.id),
        executionsApi.getRecentExecutions(activeProject?.id, 8),
        workersApi.getWorkers(),
      ]);

      setMetrics(overviewData);
      setRecentExecutions(executionsData || []);
      setWorkers(workersData || []);
    } catch (err: any) {
      console.error('Error loading dashboard overview:', err);
      setError(err?.message || 'Failed to load dashboard telemetry');
    } finally {
      setLoading(false);
    }
  }, [activeProject]);

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 6000); // Polling real state every 6s
    return () => clearInterval(interval);
  }, [loadData]);

  if (loading && !metrics) {
    return <LoadingSpinner message="Querying TaskFlow telemetry & Redis execution store..." />;
  }

  if (error && !metrics) {
    return <ErrorState message={error} onRetry={loadData} />;
  }

  return (
    <div>
      {/* Page Title */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <Activity size={22} color="var(--cyan-400)" />
            <span>System Overview</span>
          </h1>
          <p className="page-subtitle">
            Real-time execution telemetry and worker orchestration state for{' '}
            <strong style={{ color: 'var(--text-primary)' }}>
              {activeProject ? activeProject.name : 'All Projects'}
            </strong>
          </p>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <Button
            variant="secondary"
            size="sm"
            icon={<RotateCcw size={13} />}
            onClick={loadData}
          >
            Refresh State
          </Button>
          <Button
            variant="primary"
            size="sm"
            icon={<GitBranch size={13} />}
            onClick={() => onNavigate('/dashboard/workflows')}
          >
            Workflows
          </Button>
        </div>
      </div>

      {/* DLQ Alert Banner if any failed tasks exist */}
      {metrics && metrics.dlqCount > 0 && (
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '12px 16px',
            background: 'rgba(244, 63, 94, 0.1)',
            border: '1px solid rgba(244, 63, 94, 0.3)',
            borderRadius: 'var(--radius-md)',
            marginBottom: '20px',
            color: 'var(--rose-400)',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <AlertTriangle size={18} />
            <div>
              <strong style={{ fontSize: '13px' }}>
                {metrics.dlqCount} {metrics.dlqCount === 1 ? 'task' : 'tasks'} retained in Dead Letter Queue
              </strong>
              <div style={{ fontSize: '11px', color: 'var(--text-secondary)' }}>
                Tasks exhausted all retry attempts and require inspection or manual reprocessing.
              </div>
            </div>
          </div>
          <Button
            variant="danger"
            size="sm"
            onClick={() => onNavigate('/dashboard/dlq')}
            icon={<AlertOctagon size={13} />}
          >
            Inspect DLQ
          </Button>
        </div>
      )}

      {/* Real Metrics Grid */}
      {metrics && (
        <div className="metrics-grid">
          <div className="metric-card cyan">
            <div className="metric-header">
              <span>ACTIVE WORKERS</span>
              <Server size={15} color="var(--cyan-400)" />
            </div>
            <div className="metric-value">{metrics.activeWorkers}</div>
            <div className="metric-subtext">Registered HTTP worker endpoints</div>
          </div>

          <div className="metric-card cyan">
            <div className="metric-header">
              <span>RUNNING EXECUTIONS</span>
              <Play size={15} color="var(--cyan-400)" />
            </div>
            <div className="metric-value">{metrics.runningExecutions}</div>
            <div className="metric-subtext">Currently executing in background</div>
          </div>

          <div className="metric-card emerald">
            <div className="metric-header">
              <span>COMPLETED EXECUTIONS</span>
              <CheckCircle2 size={15} color="var(--emerald-400)" />
            </div>
            <div className="metric-value">{metrics.completedExecutions}</div>
            <div className="metric-subtext">Successfully resolved workflows</div>
          </div>

          <div className="metric-card rose">
            <div className="metric-header">
              <span>FAILED EXECUTIONS</span>
              <XCircle size={15} color="var(--rose-400)" />
            </div>
            <div className="metric-value">{metrics.failedExecutions}</div>
            <div className="metric-subtext">Executions with unrecoverable errors</div>
          </div>

          <div className="metric-card amber">
            <div className="metric-header">
              <span>QUEUED TASKS</span>
              <Clock size={15} color="var(--amber-400)" />
            </div>
            <div className="metric-value">{metrics.queuedTasks}</div>
            <div className="metric-subtext">Tasks pending prerequisite completion</div>
          </div>

          <div className="metric-card violet">
            <div className="metric-header">
              <span>WORKFLOWS CONFIGURED</span>
              <GitBranch size={15} color="var(--violet-400)" />
            </div>
            <div className="metric-value">{metrics.totalWorkflows}</div>
            <div className="metric-subtext">Available DAG workflow blueprints</div>
          </div>
        </div>
      )}

      {/* Main Grid: Recent Executions & Worker Fleet */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
        {/* Recent Executions */}
        <Card
          title={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Play size={16} color="var(--cyan-400)" />
              <span>Recent Workflow Executions</span>
            </div>
          }
          action={
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onNavigate('/dashboard/executions')}
            >
              View All <ArrowRight size={13} />
            </Button>
          }
        >
          {recentExecutions.length === 0 ? (
            <EmptyState
              title="No executions recorded yet"
              description="Trigger a workflow execution to begin distributed background processing."
              action={
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => onNavigate('/dashboard/workflows')}
                  icon={<Play size={13} />}
                >
                  Trigger First Workflow
                </Button>
              }
            />
          ) : (
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Workflow</th>
                    <th>Execution ID</th>
                    <th>Status</th>
                    <th>Started At</th>
                    <th>Duration</th>
                  </tr>
                </thead>
                <tbody>
                  {recentExecutions.map((exec) => (
                    <tr
                      key={exec.executionId}
                      style={{ cursor: 'pointer' }}
                      onClick={() => onNavigate(`/dashboard/executions/${exec.executionId}`)}
                    >
                      <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                        {exec.workflowId}
                      </td>
                      <td className="font-mono" style={{ fontSize: '11px', color: 'var(--cyan-400)' }}>
                        {exec.executionId.substring(0, 12)}...
                      </td>
                      <td>
                        <ExecutionStatusBadge status={exec.status} />
                      </td>
                      <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                        {exec.startedAt ? new Date(exec.startedAt).toLocaleTimeString() : '—'}
                      </td>
                      <td className="font-mono" style={{ color: 'var(--text-primary)' }}>
                        {exec.durationMs != null ? `${exec.durationMs} ms` : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>

        {/* Worker Fleet Monitor */}
        <Card
          title={
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Server size={16} color="var(--emerald-400)" />
              <span>Worker Fleet Status</span>
            </div>
          }
          action={
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onNavigate('/dashboard/workers')}
            >
              Details <ArrowRight size={13} />
            </Button>
          }
        >
          {workers.length === 0 ? (
            <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '12px' }}>
              No workers registered in WorkerRegistry.
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {workers.slice(0, 5).map((w) => (
                <div
                  key={w.workerId}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '8px 12px',
                    background: 'var(--bg-surface-elevated)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-sm)',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--emerald-400)' }} />
                    <span style={{ fontFamily: 'var(--font-mono)', fontSize: '12px', fontWeight: 600, color: 'var(--text-primary)' }}>
                      {w.workerId}
                    </span>
                  </div>
                  <span style={{ fontFamily: 'var(--font-mono)', fontSize: '10px', color: 'var(--text-muted)' }}>
                    ONLINE
                  </span>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};
