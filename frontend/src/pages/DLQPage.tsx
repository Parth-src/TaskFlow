import React, { useEffect, useState, useCallback } from 'react';
import { useProject } from '../context/ProjectContext';
import { dlqApi } from '../api/dlq';
import { DLQEntry } from '../types';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { EmptyState } from '../components/common/EmptyState';
import {
  AlertOctagon,
  RotateCcw,
  AlertTriangle,
  Server,
  CheckCircle2,
  ShieldAlert,
} from 'lucide-react';

export const DLQPage: React.FC = () => {
  const { activeProject } = useProject();
  const [entries, setEntries] = useState<DLQEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reprocessingId, setReprocessingId] = useState<string | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  const loadDLQ = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await dlqApi.getEntries();
      setEntries(data || []);
    } catch (err: any) {
      console.error('Failed to load DLQ entries:', err);
      setError(err?.message || 'Failed to query Redis DeadLetterQueue');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDLQ();
  }, [loadDLQ]);

  const handleReprocess = async (taskId: string) => {
    if (!activeProject) {
      setError('Please select an active project before reprocessing.');
      return;
    }

    try {
      setReprocessingId(taskId);
      setError(null);
      const res = await dlqApi.reprocessTask(taskId, activeProject.id);
      setActionSuccess(`Reprocessing initiated: ${res.message || 'Task sent to executor'}`);
      setTimeout(() => setActionSuccess(null), 4000);
      await loadDLQ();
    } catch (err: any) {
      setError(err?.message || 'Failed to reprocess task');
    } finally {
      setReprocessingId(null);
    }
  };

  if (loading && entries.length === 0) {
    return <LoadingSpinner message="Inspecting Redis DeadLetterQueue..." />;
  }

  if (error && entries.length === 0) {
    return <ErrorState message={error} onRetry={loadDLQ} />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <AlertOctagon size={22} color="var(--rose-400)" />
            <span>Dead Letter Queue (DLQ)</span>
          </h1>
          <p className="page-subtitle">
            Failed tasks whose retry attempts were exhausted. Retained in Redis for investigation and reprocessing.
          </p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          icon={<RotateCcw size={13} />}
          onClick={loadDLQ}
        >
          Refresh DLQ
        </Button>
      </div>

      {actionSuccess && (
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
            marginBottom: '16px',
          }}
        >
          <CheckCircle2 size={16} />
          <span>{actionSuccess}</span>
        </div>
      )}

      {entries.length === 0 ? (
        <EmptyState
          icon={CheckCircle2}
          title="Dead Letter Queue is empty"
          description="All background tasks have executed successfully or are within normal retry limits. No exhausted failures detected."
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div
            style={{
              padding: '12px 16px',
              background: 'rgba(244, 63, 94, 0.08)',
              border: '1px solid rgba(244, 63, 94, 0.25)',
              borderRadius: 'var(--radius-md)',
              display: 'flex',
              alignItems: 'center',
              gap: '12px',
            }}
          >
            <ShieldAlert size={20} color="var(--rose-400)" />
            <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
              <strong style={{ color: 'var(--rose-400)' }}>{entries.length} exhausted task failures</strong>{' '}
              retained in DLQ. You can re-dispatch tasks to workers using the Reprocess action below.
            </div>
          </div>

          <Card
            title={
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <AlertOctagon size={16} color="var(--rose-400)" />
                <span>Exhausted Task Failures ({entries.length})</span>
              </div>
            }
          >
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Task ID</th>
                    <th>Target Worker</th>
                    <th>Attempts</th>
                    <th>Failure Reason</th>
                    <th>Failed At</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {entries.map((entry) => (
                    <tr key={entry.taskId}>
                      <td className="font-mono" style={{ color: 'var(--text-primary)', fontWeight: 600 }}>
                        {entry.taskId}
                      </td>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--cyan-400)' }}>
                          <Server size={13} />
                          <span className="font-mono">{entry.workerId}</span>
                        </div>
                      </td>
                      <td className="font-mono">{entry.attemptCount} / 3</td>
                      <td>
                        <div style={{ color: 'var(--rose-400)', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                          <AlertTriangle size={13} />
                          <span className="font-mono">{entry.reason || 'Worker execution failed'}</span>
                        </div>
                      </td>
                      <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                        {entry.timestamp ? new Date(entry.timestamp).toLocaleString() : '—'}
                      </td>
                      <td>
                        <Button
                          variant="secondary"
                          size="sm"
                          icon={<RotateCcw size={12} />}
                          loading={reprocessingId === entry.taskId}
                          onClick={() => handleReprocess(entry.taskId)}
                        >
                          Reprocess
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};
