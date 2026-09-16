import React from 'react';
import { TaskExecution } from '../../types';
import { ExecutionStatusBadge } from './ExecutionStatusBadge';
import { Server, AlertCircle } from 'lucide-react';

interface TaskExecutionTableProps {
  tasks: TaskExecution[];
}

export const TaskExecutionTable: React.FC<TaskExecutionTableProps> = ({ tasks }) => {
  return (
    <div className="table-container">
      <table>
        <thead>
          <tr>
            <th>Task ID</th>
            <th>Worker</th>
            <th>Status</th>
            <th>Attempt</th>
            <th>Started At</th>
            <th>Completed At</th>
            <th>Duration</th>
            <th>Error / Reason</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map((task) => (
            <tr key={task.taskId}>
              <td className="font-mono" style={{ color: 'var(--text-primary)', fontWeight: 600 }}>
                {task.taskId}
              </td>
              <td>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--cyan-400)' }}>
                  <Server size={13} />
                  <span className="font-mono">{task.workerId}</span>
                </div>
              </td>
              <td>
                <ExecutionStatusBadge status={task.status} />
              </td>
              <td className="font-mono">{task.attempt}</td>
              <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                {task.startedAt ? new Date(task.startedAt).toLocaleTimeString() : '—'}
              </td>
              <td className="font-mono" style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                {task.completedAt ? new Date(task.completedAt).toLocaleTimeString() : '—'}
              </td>
              <td className="font-mono" style={{ color: 'var(--text-primary)' }}>
                {task.durationMs != null ? `${task.durationMs} ms` : '—'}
              </td>
              <td>
                {task.error ? (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--rose-400)', fontSize: '12px' }}>
                    <AlertCircle size={13} />
                    <span className="font-mono">{task.error}</span>
                  </div>
                ) : (
                  <span style={{ color: 'var(--text-muted)' }}>—</span>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
