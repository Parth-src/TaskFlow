import React from 'react';
import { WorkflowSummary } from '../../types';
import { GitBranch, ArrowRight, Play, Server } from 'lucide-react';
import { Button } from '../common/Button';

interface WorkflowCardProps {
  workflow: WorkflowSummary;
  onSelect: (workflow: WorkflowSummary) => void;
  onTrigger: (workflow: WorkflowSummary) => void;
}

export const WorkflowCard: React.FC<WorkflowCardProps> = ({
  workflow,
  onSelect,
  onTrigger,
}) => {
  return (
    <div
      className="card"
      style={{
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
        cursor: 'pointer',
      }}
      onClick={() => onSelect(workflow)}
    >
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <div
              style={{
                width: '32px',
                height: '32px',
                borderRadius: '6px',
                background: 'rgba(6, 182, 212, 0.1)',
                border: '1px solid rgba(6, 182, 212, 0.25)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--cyan-400)',
              }}
            >
              <GitBranch size={16} />
            </div>
            <div>
              <h4 style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-primary)' }}>
                {workflow.name}
              </h4>
              <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                {workflow.taskCount} tasks defined
              </span>
            </div>
          </div>
        </div>

        {/* Task Preview Chips */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', margin: '14px 0' }}>
          {workflow.tasks.map((task) => (
            <span
              key={task.id}
              style={{
                fontSize: '11px',
                fontFamily: 'var(--font-mono)',
                padding: '2px 8px',
                background: 'var(--bg-surface-elevated)',
                border: '1px solid var(--border-subtle)',
                borderRadius: '4px',
                color: 'var(--text-secondary)',
                display: 'flex',
                alignItems: 'center',
                gap: '4px',
              }}
            >
              <Server size={10} color="var(--text-muted)" />
              {task.id}
            </span>
          ))}
        </div>
      </div>

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          paddingTop: '12px',
          borderTop: '1px solid var(--border-subtle)',
        }}
      >
        <span
          style={{
            fontSize: '12px',
            color: 'var(--cyan-400)',
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            fontWeight: 500,
          }}
        >
          View DAG Graph <ArrowRight size={13} />
        </span>
        <Button
          variant="primary"
          size="sm"
          icon={<Play size={12} />}
          onClick={(e) => {
            e.stopPropagation();
            onTrigger(workflow);
          }}
        >
          Run Workflow
        </Button>
      </div>
    </div>
  );
};
