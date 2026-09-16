import React, { useMemo, useState } from 'react';
import { WorkflowTask, TaskExecution } from '../../types';
import { Server, CheckCircle2, XCircle, Play, Clock, RotateCcw } from 'lucide-react';

interface WorkflowGraphProps {
  tasks: WorkflowTask[];
  taskExecutions?: TaskExecution[];
  onSelectTask?: (task: WorkflowTask) => void;
  selectedTaskId?: string;
}

interface NodeLayout {
  task: WorkflowTask;
  execution?: TaskExecution;
  level: number;
  indexInLevel: number;
  x: number;
  y: number;
}

export const WorkflowGraph: React.FC<WorkflowGraphProps> = ({
  tasks,
  taskExecutions = [],
  onSelectTask,
  selectedTaskId,
}) => {
  const [hoveredNode, setHoveredNode] = useState<string | null>(null);

  const executionMap = useMemo(() => {
    const map = new Map<string, TaskExecution>();
    taskExecutions.forEach((exec) => {
      map.set(exec.taskId, exec);
      map.set(exec.workerId, exec);
    });
    return map;
  }, [taskExecutions]);

  // Topological leveling
  const { nodes, edges, width, height } = useMemo(() => {
    if (!tasks || tasks.length === 0) {
      return { nodes: [], edges: [], width: 600, height: 300 };
    }

    const taskMap = new Map<string, WorkflowTask>();
    tasks.forEach((t) => taskMap.set(t.id, t));

    const levels = new Map<string, number>();

    const getLevel = (id: string, visited: Set<string> = new Set()): number => {
      if (visited.has(id)) return 0;
      visited.add(id);
      if (levels.has(id)) return levels.get(id)!;

      const t = taskMap.get(id);
      if (!t || !t.dependsOn || t.dependsOn.length === 0) {
        levels.set(id, 0);
        return 0;
      }

      const parentLevels = t.dependsOn.map((p) => getLevel(p, new Set(visited)));
      const maxParent = Math.max(...parentLevels, 0);
      const lvl = maxParent + 1;
      levels.set(id, lvl);
      return lvl;
    };

    tasks.forEach((t) => getLevel(t.id));

    // Group by level
    const levelGroups = new Map<number, WorkflowTask[]>();
    tasks.forEach((t) => {
      const lvl = levels.get(t.id) || 0;
      if (!levelGroups.has(lvl)) levelGroups.set(lvl, []);
      levelGroups.get(lvl)!.push(t);
    });

    const maxLevel = Math.max(...Array.from(levelGroups.keys()), 0);
    const nodeWidth = 180;
    const nodeHeight = 70;
    const horizontalGap = 100;
    const verticalGap = 40;

    let maxNodesInLevel = 0;
    levelGroups.forEach((group) => {
      if (group.length > maxNodesInLevel) maxNodesInLevel = group.length;
    });

    const calculatedHeight = Math.max(340, maxNodesInLevel * (nodeHeight + verticalGap) + 60);
    const calculatedWidth = Math.max(640, (maxLevel + 1) * (nodeWidth + horizontalGap) + 80);

    const layoutNodes: NodeLayout[] = [];
    levelGroups.forEach((group, lvl) => {
      const totalGroupHeight = group.length * nodeHeight + (group.length - 1) * verticalGap;
      const startY = (calculatedHeight - totalGroupHeight) / 2;

      group.forEach((task, idx) => {
        const x = 50 + lvl * (nodeWidth + horizontalGap);
        const y = startY + idx * (nodeHeight + verticalGap);
        layoutNodes.push({
          task,
          execution: executionMap.get(task.id) || executionMap.get(task.worker),
          level: lvl,
          indexInLevel: idx,
          x,
          y,
        });
      });
    });

    const layoutMap = new Map<string, NodeLayout>();
    layoutNodes.forEach((n) => layoutMap.set(n.task.id, n));

    const edgeList: { from: NodeLayout; to: NodeLayout; active: boolean }[] = [];
    tasks.forEach((task) => {
      const toNode = layoutMap.get(task.id);
      if (!toNode || !task.dependsOn) return;

      task.dependsOn.forEach((depId) => {
        const fromNode = layoutMap.get(depId);
        if (fromNode) {
          const isFromCompleted =
            fromNode.execution?.status?.toUpperCase() === 'COMPLETED';
          edgeList.push({
            from: fromNode,
            to: toNode,
            active: isFromCompleted,
          });
        }
      });
    });

    return {
      nodes: layoutNodes,
      edges: edgeList,
      width: calculatedWidth,
      height: calculatedHeight,
    };
  }, [tasks, executionMap]);

  return (
    <div className="dag-container">
      <div
        style={{
          position: 'absolute',
          top: '12px',
          right: '16px',
          display: 'flex',
          gap: '12px',
          zIndex: 10,
          background: 'rgba(5, 8, 14, 0.85)',
          padding: '6px 12px',
          borderRadius: 'var(--radius-sm)',
          border: '1px solid var(--border-subtle)',
          fontSize: '11px',
          color: 'var(--text-secondary)',
        }}
      >
        <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--cyan-400)' }} />
          Running
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--emerald-400)' }} />
          Completed
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--rose-400)' }} />
          Failed
        </span>
        <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
          <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: 'var(--amber-400)' }} />
          Retrying
        </span>
      </div>

      <svg
        className="dag-svg-canvas"
        viewBox={`0 0 ${width} ${height}`}
        preserveAspectRatio="xMidYMid meet"
      >
        <defs>
          <linearGradient id="edge-active-grad" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" stopColor="#06b6d4" />
            <stop offset="100%" stopColor="#22d3ee" />
          </linearGradient>
          <filter id="glow-cyan" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur stdDeviation="3" result="blur" />
            <feComposite in="SourceGraphic" in2="blur" operator="over" />
          </filter>
        </defs>

        {/* Edges */}
        {edges.map(({ from, to, active }, idx) => {
          const startX = from.x + 180;
          const startY = from.y + 35;
          const endX = to.x;
          const endY = to.y + 35;
          const control1X = startX + (endX - startX) * 0.5;
          const control2X = endX - (endX - startX) * 0.5;

          const pathD = `M ${startX} ${startY} C ${control1X} ${startY}, ${control2X} ${endY}, ${endX} ${endY}`;

          return (
            <g key={`edge-${idx}`}>
              <path
                d={pathD}
                className={`dag-edge ${active ? 'active' : ''}`}
                stroke={active ? 'url(#edge-active-grad)' : 'rgba(255, 255, 255, 0.15)'}
              />
              {active && (
                <circle r="3" fill="#22d3ee" filter="url(#glow-cyan)">
                  <animateMotion path={pathD} dur="2.5s" repeatCount="indefinite" />
                </circle>
              )}
            </g>
          );
        })}

        {/* Nodes */}
        {nodes.map(({ task, execution, x, y }) => {
          const isSelected = selectedTaskId === task.id;
          const isHovered = hoveredNode === task.id;
          const normStatus = (execution?.status || '').toUpperCase();

          let nodeStroke = 'rgba(255, 255, 255, 0.12)';
          let nodeFill = '#0d131f';
          let statusColor = 'var(--text-muted)';
          let StatusIcon = Clock;

          if (normStatus === 'RUNNING') {
            nodeStroke = '#06b6d4';
            nodeFill = '#081a28';
            statusColor = '#22d3ee';
            StatusIcon = Play;
          } else if (normStatus === 'COMPLETED') {
            nodeStroke = '#10b981';
            nodeFill = '#071f18';
            statusColor = '#34d399';
            StatusIcon = CheckCircle2;
          } else if (normStatus === 'FAILED') {
            nodeStroke = '#f43f5e';
            nodeFill = '#220b12';
            statusColor = '#fb7185';
            StatusIcon = XCircle;
          } else if (normStatus === 'RETRYING') {
            nodeStroke = '#f59e0b';
            nodeFill = '#221808';
            statusColor = '#fbbf24';
            StatusIcon = RotateCcw;
          }

          if (isSelected) {
            nodeStroke = '#8b5cf6';
            nodeFill = '#17112b';
          }

          return (
            <g
              key={task.id}
              className="dag-node"
              transform={`translate(${x}, ${y})`}
              onClick={() => onSelectTask?.(task)}
              onMouseEnter={() => setHoveredNode(task.id)}
              onMouseLeave={() => setHoveredNode(null)}
            >
              {/* Card Box */}
              <rect
                width="180"
                height="70"
                fill={nodeFill}
                stroke={nodeStroke}
                strokeWidth={isSelected || isHovered ? 2 : 1.2}
                rx="6"
                ry="6"
                style={{
                  filter:
                    normStatus === 'RUNNING'
                      ? 'drop-shadow(0 0 8px rgba(6,182,212,0.3))'
                      : isSelected
                      ? 'drop-shadow(0 0 8px rgba(139,92,246,0.3))'
                      : 'none',
                }}
              />

              {/* Task Name */}
              <text
                x="14"
                y="26"
                fill="#f8fafc"
                fontSize="13"
                fontWeight="600"
                fontFamily="var(--font-sans)"
              >
                {task.id}
              </text>

              {/* Worker Subtitle */}
              <g transform="translate(14, 40)">
                <text
                  x="0"
                  y="12"
                  fill="var(--text-muted)"
                  fontSize="10"
                  fontFamily="var(--font-mono)"
                >
                  worker: {task.worker}
                </text>
              </g>

              {/* Status Indicator */}
              <g transform="translate(148, 16)">
                <StatusIcon size={14} color={statusColor} />
              </g>

              {/* Duration or Attempt badge */}
              {execution && (
                <g transform="translate(14, 60)">
                  <text
                    x="0"
                    y="0"
                    fill={statusColor}
                    fontSize="9.5"
                    fontFamily="var(--font-mono)"
                    fontWeight="600"
                  >
                    {normStatus || 'PENDING'} {execution.durationMs ? `(${execution.durationMs}ms)` : ''}
                  </text>
                </g>
              )}
            </g>
          );
        })}
      </svg>
    </div>
  );
};
