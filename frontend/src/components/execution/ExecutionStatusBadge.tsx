import React from 'react';
import { ExecutionStatus } from '../../types';
import { Play, CheckCircle2, XCircle, RotateCcw, Clock } from 'lucide-react';

interface ExecutionStatusBadgeProps {
  status: ExecutionStatus | string;
  showIcon?: boolean;
}

export const ExecutionStatusBadge: React.FC<ExecutionStatusBadgeProps> = ({
  status,
  showIcon = true,
}) => {
  const norm = (status || '').toUpperCase();

  let className = 'badge badge-pending';
  let icon = <Clock size={11} />;

  if (norm === 'RUNNING') {
    className = 'badge badge-running';
    icon = <Play size={11} className="pulse" />;
  } else if (norm === 'COMPLETED' || norm === 'SUCCESS') {
    className = 'badge badge-completed';
    icon = <CheckCircle2 size={11} />;
  } else if (norm === 'FAILED' || norm === 'ERROR') {
    className = 'badge badge-failed';
    icon = <XCircle size={11} />;
  } else if (norm === 'RETRYING') {
    className = 'badge badge-retrying';
    icon = <RotateCcw size={11} />;
  } else if (norm === 'READY' || norm === 'PENDING' || norm === 'QUEUED') {
    className = 'badge badge-pending';
    icon = <Clock size={11} />;
  }

  return (
    <span className={className}>
      {showIcon && icon}
      <span>{norm}</span>
    </span>
  );
};
