import React, { useEffect, useState, useCallback } from 'react';
import { workersApi } from '../api/workers';
import { WorkerMetadata } from '../types';
import { Card } from '../components/common/Card';
import { Button } from '../components/common/Button';
import { LoadingSpinner } from '../components/common/LoadingSpinner';
import { ErrorState } from '../components/common/ErrorState';
import { EmptyState } from '../components/common/EmptyState';
import { Server, RotateCcw, CheckCircle2, Globe, Shield, Code2 } from 'lucide-react';
import { CodeBlock } from '../components/common/CodeBlock';

export const WorkersPage: React.FC = () => {
  const [workers, setWorkers] = useState<WorkerMetadata[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadWorkers = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await workersApi.getWorkers();
      setWorkers(data || []);
    } catch (err: any) {
      console.error('Failed to load workers:', err);
      setError(err?.message || 'Failed to query WorkerRegistry');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadWorkers();
  }, [loadWorkers]);

  const workerContract = `// Worker HTTP Contract Example
POST /workers/payment
Authorization: Bearer <TASKFLOW_WORKER_TOKEN>
Content-Type: application/json

// Response Contract
HTTP/1.1 200 OK
Content-Type: application/json

{
  "success": true,
  "message": "Payment captured successfully",
  "shouldRetry": false
}`;

  if (loading && workers.length === 0) {
    return <LoadingSpinner message="Querying active workers from WorkerRegistry..." />;
  }

  if (error && workers.length === 0) {
    return <ErrorState message={error} onRetry={loadWorkers} />;
  }

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <h1 className="page-title">
            <Server size={22} color="var(--emerald-400)" />
            <span>Worker Fleet</span>
          </h1>
          <p className="page-subtitle">
            Language-independent HTTP workers registered to execute background workflow tasks.
          </p>
        </div>
        <Button
          variant="secondary"
          size="sm"
          icon={<RotateCcw size={13} />}
          onClick={loadWorkers}
        >
          Refresh Fleet
        </Button>
      </div>

      {workers.length === 0 ? (
        <EmptyState
          icon={Server}
          title="No registered workers found"
          description="Workers register automatically or via WorkerDiscovery configuration."
        />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
          {/* Workers Table */}
          <Card
            title={
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Server size={16} color="var(--emerald-400)" />
                <span>Registered Workers ({workers.length})</span>
              </div>
            }
            subtitle="Endpoints polled and invoked by TaskFlow HttpDispatcher."
          >
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>Worker ID</th>
                    <th>Status</th>
                    <th>HTTP Endpoint URL</th>
                  </tr>
                </thead>
                <tbody>
                  {workers.map((w) => (
                    <tr key={w.workerId}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'var(--emerald-400)' }} />
                          <strong className="font-mono" style={{ color: 'var(--text-primary)' }}>
                            {w.workerId}
                          </strong>
                        </div>
                      </td>
                      <td>
                        <span className="badge badge-completed">
                          <CheckCircle2 size={11} /> ONLINE
                        </span>
                      </td>
                      <td className="font-mono" style={{ fontSize: '11px', color: 'var(--cyan-400)' }}>
                        {w.endpoint}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </Card>

          {/* Architecture & Worker Contract Info */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <Card
              title={
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Shield size={16} color="var(--cyan-400)" />
                  <span>Language Independence</span>
                </div>
              }
            >
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.6, marginBottom: '12px' }}>
                TaskFlow dispatches work over standard HTTP. Workers can be implemented in Java, Python, Go, Node.js, C#, or Rust.
              </p>
              <div style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
                Each worker receives an authorization token and returns a standardized JSON status payload.
              </div>
            </Card>

            <Card
              title={
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Code2 size={16} color="var(--violet-400)" />
                  <span>Standard Worker Contract</span>
                </div>
              }
            >
              <CodeBlock code={workerContract} language="http" title="HTTP Contract" />
            </Card>
          </div>
        </div>
      )}
    </div>
  );
};
