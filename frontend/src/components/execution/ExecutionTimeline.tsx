import React from 'react';
import { TaskExecution } from '../../types';

interface ExecutionTimelineProps {
  tasks: TaskExecution[];
}

export const ExecutionTimeline: React.FC<ExecutionTimelineProps> = ({ tasks }) => {
  if (tasks.length === 0) return null;

  // Calculate timeline bounds
  let minStart = Infinity;
  let maxEnd = -Infinity;

  const validTasks = tasks.filter((t) => t.startedAt);

  if (validTasks.length === 0) {
    return (
      <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '13px' }}>
        Timeline data will be rendered once tasks begin execution.
      </div>
    );
  }

  validTasks.forEach((t) => {
    const s = new Date(t.startedAt!).getTime();
    const c = t.completedAt ? new Date(t.completedAt).getTime() : Date.now();
    if (s < minStart) minStart = s;
    if (c > maxEnd) maxEnd = c;
  });

  const totalDuration = Math.max(maxEnd - minStart, 100);

  return (
    <div style={{ padding: '8px 0' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', marginBottom: '12px' }}>
        <span>0 ms (Start)</span>
        <span>{totalDuration} ms (Total Span)</span>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
        {tasks.map((task) => {
          if (!task.startedAt) {
            return (
              <div key={task.taskId} style={{ display: 'grid', gridTemplateColumns: '160px 1fr', alignItems: 'center', gap: '16px' }}>
                <div style={{ fontSize: '12px', fontFamily: 'var(--font-mono)', color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {task.workerId}
                </div>
                <div style={{ height: '24px', background: 'rgba(255,255,255,0.02)', borderRadius: '4px', display: 'flex', alignItems: 'center', paddingLeft: '8px', fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                  Queued / Waiting for dependencies
                </div>
              </div>
            );
          }

          const start = new Date(task.startedAt).getTime();
          const end = task.completedAt ? new Date(task.completedAt).getTime() : Date.now();
          const duration = Math.max(end - start, 50);

          const leftPercent = Math.max(0, Math.min(100, ((start - minStart) / totalDuration) * 100));
          const widthPercent = Math.max(1, Math.min(100 - leftPercent, (duration / totalDuration) * 100));

          const normStatus = (task.status || '').toUpperCase();
          let barBg = 'linear-gradient(90deg, #06b6d4, #22d3ee)';
          if (normStatus === 'COMPLETED') barBg = 'linear-gradient(90deg, #10b981, #34d399)';
          if (normStatus === 'FAILED') barBg = 'linear-gradient(90deg, #f43f5e, #fb7185)';
          if (normStatus === 'RETRYING') barBg = 'linear-gradient(90deg, #f59e0b, #fbbf24)';

          return (
            <div key={task.taskId} style={{ display: 'grid', gridTemplateColumns: '160px 1fr', alignItems: 'center', gap: '16px' }}>
              <div style={{ fontSize: '12px', fontFamily: 'var(--font-mono)', color: 'var(--text-primary)', fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {task.workerId}
              </div>
              <div className="timeline-track">
                <div
                  className="timeline-bar"
                  style={{
                    left: `${leftPercent}%`,
                    width: `${widthPercent}%`,
                    background: barBg,
                    boxShadow: '0 0 10px rgba(0,0,0,0.5)',
                  }}
                >
                  <span style={{ fontSize: '10px' }}>{duration}ms</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
