import React from 'react';
import { AlertTriangle, RotateCcw } from 'lucide-react';
import { Button } from './Button';

interface ErrorStateProps {
  title?: string;
  message: string;
  onRetry?: () => void;
}

export const ErrorState: React.FC<ErrorStateProps> = ({
  title = 'Failed to load resource',
  message,
  onRetry,
}) => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '48px 24px',
        textAlign: 'center',
        border: '1px solid rgba(244, 63, 94, 0.2)',
        borderRadius: 'var(--radius-md)',
        background: 'rgba(244, 63, 94, 0.05)',
      }}
    >
      <div
        style={{
          width: '40px',
          height: '40px',
          borderRadius: '50%',
          backgroundColor: 'rgba(244, 63, 94, 0.1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'var(--rose-400)',
          marginBottom: '14px',
        }}
      >
        <AlertTriangle size={20} />
      </div>
      <h4 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)', marginBottom: '4px' }}>
        {title}
      </h4>
      <p style={{ fontSize: '13px', color: 'var(--text-secondary)', maxWidth: '420px', marginBottom: onRetry ? '18px' : 0 }}>
        {message}
      </p>
      {onRetry && (
        <Button variant="secondary" size="sm" icon={<RotateCcw size={14} />} onClick={onRetry}>
          Try Again
        </Button>
      )}
    </div>
  );
};
