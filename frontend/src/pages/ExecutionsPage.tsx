import React, { useEffect, useState, useCallback } from 'react';
import { useProject } from '../context/ProjectContext';
import { executionsApi } from '../api/executions';
import { ExecutionSummary } from '../types';
import { ExecutionStatusBadge } from '../components/execution/ExecutionStatusBadge';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { EmptyState } from '../components/common/EmptyState';
import { PlaySquare, RotateCcw, Play, Filter, Clock } from 'lucide-react';

interface ExecutionsPageProps {
  onNavigate: (path: string) => void;
}

export const ExecutionsPage: React.FC<ExecutionsPageProps> = ({ onNavigate }) => {
  const { activeProject } = useProject();
  const [executions, setExecutions] = useState<ExecutionSummary[]>([]);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [loading, setLoading] = useState(true);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadExecutions = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await executionsApi.getRecentExecutions(activeProject?.id, 50);
      setExecutions(data || []);
    } catch (err: any) {
      console.error('Failed to load executions:', err);
      setError(err?.message || 'Failed to load executions');
    } finally {
      setLoading(false);
    }
  }, [activeProject]);

  useEffect(() => {
    loadExecutions();
  }, [loadExecutions]);

  useEffect(() => {
    if (!autoRefresh) return;
    const interval = setInterval(loadExecutions, 5000);
    return () => clearInterval(interval);
  }, [autoRefresh, loadExecutions]);

  const filteredExecutions = executions.filter((exec) => {
    if (filterStatus === 'ALL') return true;
    return (exec.status || '').toUpperCase() === filterStatus;
  });

  const statuses = ['ALL', 'RUNNING', 'COMPLETED', 'FAILED', 'RETRYING', 'PENDING'];

  if (loading && executions.length === 0) {
    return <LoadingSpinner message="Fetching execution history from Redis ExecutionStore..." />;
  }

  if (error && executions.length === 0) {
    return <ErrorState message={error} onRetry={loadExecutions} />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <PlaySquare size={22} color="var(--cyan-400)" />
            <span>Workflow Executions</span>
          </h1>
          <p className="page-subtitle">
            Lifecycle history of background executions, state transitions, and task telemetry.
          </p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <button
            className={`btn btn-sm ${autoRefresh ? 'btn-primary' : 'btn-secondary'}`}
            onClick={() => setAutoRefresh(!autoRefresh)}
          >
            <Clock size={12} />
            <span>Live Polling {autoRefresh ? 'ON' : 'OFF'}</span>
          </button>
          <Button
            variant="secondary"
            size="sm"
            icon={<RotateCcw size={13} />}
            onClick={loadExecutions}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            size="sm"
            icon={<Play size={13} />}
            onClick={() => onNavigate('/dashboard/workflows')}
          >
            Trigger Execution
          </Button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div style={{ display: 'flex', gap: '6px', marginBottom: '20px', flexWrap: 'wrap' }}>
        {statuses.map((status) => {
          const isActive = filterStatus === status;
          return (
            <button
              key={status}
              onClick={() => setFilterStatus(status)}
              style={{
                padding: '5px 12px',
                borderRadius: 'var(--radius-sm)',
                fontSize: '12px',
                fontFamily: 'var(--font-mono)',
                fontWeight: isActive ? 600 : 400,
                background: isActive ? 'var(--cyan-500)' : 'var(--bg-surface-elevated)',
                color: isActive ? '#041017' : 'var(--text-secondary)',
                border: '1px solid ' + (isActive ? 'var(--cyan-500)' : 'var(--border-subtle)'),
                transition: 'all 0.15s ease',
              }}
            >
              {status}
            </button>
          );
        })}
      </div>

      {filteredExecutions.length === 0 ? (
        <EmptyState
          icon={PlaySquare}
          title="No executions match filter"
          description={
            executions.length === 0
              ? 'No background executions have been dispatched for this project yet.'
              : `No executions found with status '${filterStatus}'.`
          }
          action={
            executions.length === 0 ? (
              <Button
                variant="primary"
                onClick={() => onNavigate('/dashboard/workflows')}
                icon={<Play size={14} />}
              >
                Run a Workflow
              </Button>
            ) : undefined
          }
        />
      ) : (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Workflow Name</th>
                <th>Execution ID</th>
                <th>Status</th>
                <th>Started At</th>
                <th>Completed At</th>
                <th>Duration</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredExecutions.map((exec) => (
                <tr
                  key={exec.executionId}
                  style={{ cursor: 'pointer' }}
                  onClick={() => onNavigate(`/dashboard/executions/${exec.executionId}`)}
                >
                  <td style={{ fontWeight: 600, color: 'var(--text-primary)' }}>
                    {exec.workflowId}
                  </td>
                  <td className="font-mono" style={{ fontSize: '11px', color: 'var(--cyan-400)' }}>
                    {exec.executionId}
                  </td>
                  <td>
                    <ExecutionStatusBadge status={exec.status} />
                  </td>
                  <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                    {exec.startedAt ? new Date(exec.startedAt).toLocaleString() : '—'}
                  </td>
                  <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                    {exec.completedAt ? new Date(exec.completedAt).toLocaleTimeString() : '—'}
                  </td>
                  <td className="font-mono" style={{ color: 'var(--text-primary)', fontWeight: 500 }}>
                    {exec.durationMs != null ? `${exec.durationMs} ms` : '—'}
                  </td>
                  <td>
                    <span style={{ fontSize: '12px', color: 'var(--cyan-400)', fontWeight: 500 }}>
                      Inspect →
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
