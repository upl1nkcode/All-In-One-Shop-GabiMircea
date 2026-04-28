/**
 * LiveFeedPage — Silver Challenge
 *
 * Real-time view of faker-generated products:
 * - Start / Stop controls for the async generator
 * - Master/Detail layout: product list + detail panel
 * - Charts: product count over time, category distribution (via recharts)
 * - WebSocket-powered live updates
 */
import { useState, useMemo, useCallback } from 'react';
import { useWebSocket, type WebSocketMessage } from '../api/useWebSocket';
import { API_BASE_URL, getAuthToken } from '../api/client';
import { toast } from 'sonner';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';

/* ─── Types ──────────────────────────────────────────────── */

interface FakeProduct {
  id: string;
  name: string;
  description?: string;
  imageUrl?: string;
  brand?: { name: string };
  category?: { name: string };
  lowestPrice?: number;
  gender?: string;
}

/* ─── Helpers ────────────────────────────────────────────── */

const COLORS = [
  '#6366f1', '#8b5cf6', '#a855f7', '#ec4899', '#f43f5e',
  '#f97316', '#eab308', '#22c55e', '#14b8a6', '#06b6d4',
];

async function fakerRequest(action: 'start' | 'stop', params?: Record<string, string>) {
  const url = new URL(`${API_BASE_URL}/faker/${action}`);
  if (params) {
    for (const [k, v] of Object.entries(params)) url.searchParams.set(k, v);
  }
  const headers: Record<string, string> = { 'Content-Type': 'application/json' };
  const token = getAuthToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(url.toString(), { method: 'POST', headers });
  if (!res.ok) throw new Error('Faker request failed');
  return res.json();
}

/* ─── Component ──────────────────────────────────────────── */

export function LiveFeedPage() {
  const [wsActive, setWsActive] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [intervalMs, setIntervalMs] = useState(3000);
  const [batchSize, setBatchSize] = useState(5);
  const [selectedProduct, setSelectedProduct] = useState<FakeProduct | null>(null);

  const { connected, messages, clearMessages } = useWebSocket(wsActive);

  // ── Derived data ──────────────────────────────────────────

  const allProducts = useMemo(() => {
    const products: FakeProduct[] = [];
    for (const msg of messages) {
      if (msg.type === 'BATCH_ADDED' && Array.isArray(msg.products)) {
        products.push(...(msg.products as FakeProduct[]));
      }
    }
    return products;
  }, [messages]);

  // Timeline data for LineChart
  const timelineData = useMemo(() => {
    let running = 0;
    return messages
      .filter(m => m.type === 'BATCH_ADDED')
      .map((m, i) => {
        running += m.count ?? 0;
        return {
          batch: i + 1,
          total: m.totalProducts ?? running,
          batchCount: m.count ?? 0,
          time: m.timestamp ? new Date(m.timestamp).toLocaleTimeString() : `#${i + 1}`,
        };
      });
  }, [messages]);

  // Category distribution for PieChart
  const categoryData = useMemo(() => {
    const counts: Record<string, number> = {};
    for (const p of allProducts) {
      const cat = p.category?.name || 'Unknown';
      counts[cat] = (counts[cat] || 0) + 1;
    }
    return Object.entries(counts)
      .map(([name, value]) => ({ name, value }))
      .sort((a, b) => b.value - a.value)
      .slice(0, 10);
  }, [allProducts]);

  // ── Handlers ──────────────────────────────────────────────

  const handleStart = useCallback(async () => {
    try {
      setWsActive(true);
      await fakerRequest('start', {
        intervalMs: String(intervalMs),
        batchSize: String(batchSize),
      });
      setGenerating(true);
      toast.success('Faker generation started');
    } catch {
      toast.error('Failed to start generation');
    }
  }, [intervalMs, batchSize]);

  const handleStop = useCallback(async () => {
    try {
      await fakerRequest('stop');
      setGenerating(false);
      toast.info('Faker generation stopped');
    } catch {
      toast.error('Failed to stop generation');
    }
  }, []);

  const handleReset = useCallback(() => {
    clearMessages();
    setSelectedProduct(null);
  }, [clearMessages]);

  // ── Styles (inline for self-containment) ──────────────────

  const card: React.CSSProperties = {
    background: 'rgba(255,255,255,0.05)',
    borderRadius: 12,
    border: '1px solid rgba(255,255,255,0.1)',
    padding: 20,
  };

  /* ── Render ─────────────────────────────────────────────── */

  return (
    <div
      style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)',
        color: '#e2e8f0',
        fontFamily: 'Inter, system-ui, sans-serif',
      }}
    >
      {/* Header */}
      <header
        style={{
          padding: '20px 32px',
          borderBottom: '1px solid rgba(255,255,255,0.08)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          gap: 12,
        }}
      >
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, margin: 0 }}>
            ⚡ Live Product Feed
          </h1>
          <p style={{ fontSize: 13, color: '#94a3b8', margin: '4px 0 0' }}>
            Silver Challenge — Faker + WebSocket real-time stream
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
          {/* Status badge */}
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 6,
              fontSize: 13,
              padding: '4px 12px',
              borderRadius: 999,
              background: connected ? 'rgba(34,197,94,0.15)' : 'rgba(239,68,68,0.15)',
              color: connected ? '#22c55e' : '#ef4444',
              border: `1px solid ${connected ? 'rgba(34,197,94,0.3)' : 'rgba(239,68,68,0.3)'}`,
            }}
          >
            <span style={{ width: 8, height: 8, borderRadius: '50%', background: 'currentColor' }} />
            {connected ? 'Connected' : 'Disconnected'}
          </span>

          {/* Config */}
          <label style={{ fontSize: 13, display: 'flex', alignItems: 'center', gap: 4 }}>
            Interval (ms)
            <input
              type="number"
              value={intervalMs}
              onChange={e => setIntervalMs(Number(e.target.value) || 3000)}
              min={500}
              step={500}
              style={{
                width: 70,
                padding: '4px 8px',
                borderRadius: 6,
                border: '1px solid rgba(255,255,255,0.2)',
                background: 'rgba(255,255,255,0.05)',
                color: '#e2e8f0',
                fontSize: 13,
              }}
            />
          </label>
          <label style={{ fontSize: 13, display: 'flex', alignItems: 'center', gap: 4 }}>
            Batch
            <input
              type="number"
              value={batchSize}
              onChange={e => setBatchSize(Number(e.target.value) || 5)}
              min={1}
              max={50}
              style={{
                width: 50,
                padding: '4px 8px',
                borderRadius: 6,
                border: '1px solid rgba(255,255,255,0.2)',
                background: 'rgba(255,255,255,0.05)',
                color: '#e2e8f0',
                fontSize: 13,
              }}
            />
          </label>

          {/* Action buttons */}
          {!generating ? (
            <button
              onClick={handleStart}
              style={{
                padding: '8px 20px',
                borderRadius: 8,
                border: 'none',
                background: 'linear-gradient(135deg, #6366f1, #8b5cf6)',
                color: 'white',
                fontWeight: 600,
                fontSize: 14,
                cursor: 'pointer',
              }}
            >
              ▶ Start
            </button>
          ) : (
            <button
              onClick={handleStop}
              style={{
                padding: '8px 20px',
                borderRadius: 8,
                border: 'none',
                background: 'linear-gradient(135deg, #ef4444, #f97316)',
                color: 'white',
                fontWeight: 600,
                fontSize: 14,
                cursor: 'pointer',
              }}
            >
              ■ Stop
            </button>
          )}
          <button
            onClick={handleReset}
            style={{
              padding: '8px 16px',
              borderRadius: 8,
              border: '1px solid rgba(255,255,255,0.2)',
              background: 'transparent',
              color: '#94a3b8',
              fontSize: 14,
              cursor: 'pointer',
            }}
          >
            Reset
          </button>
          <a
            href="/"
            style={{
              padding: '8px 16px',
              borderRadius: 8,
              border: '1px solid rgba(255,255,255,0.2)',
              background: 'transparent',
              color: '#94a3b8',
              fontSize: 14,
              cursor: 'pointer',
              textDecoration: 'none',
            }}
          >
            ← Back
          </a>
        </div>
      </header>

      {/* Main content */}
      <div style={{ padding: '24px 32px', display: 'flex', flexDirection: 'column', gap: 24 }}>
        {/* Charts row */}
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: 24 }}>
          {/* Product count over time */}
          <div style={card}>
            <h3 style={{ margin: '0 0 12px', fontSize: 16, fontWeight: 600 }}>
              📈 Products Over Time
            </h3>
            {timelineData.length === 0 ? (
              <p style={{ color: '#64748b', fontSize: 14, textAlign: 'center', padding: 40 }}>
                Start the generator to see data…
              </p>
            ) : (
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={timelineData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                  <XAxis dataKey="time" stroke="#64748b" fontSize={11} />
                  <YAxis stroke="#64748b" fontSize={11} />
                  <Tooltip
                    contentStyle={{
                      background: '#1e293b',
                      border: '1px solid rgba(255,255,255,0.1)',
                      borderRadius: 8,
                      color: '#e2e8f0',
                    }}
                  />
                  <Line
                    type="monotone"
                    dataKey="total"
                    stroke="#6366f1"
                    strokeWidth={2}
                    dot={false}
                    name="Total Products"
                  />
                  <Line
                    type="monotone"
                    dataKey="batchCount"
                    stroke="#22c55e"
                    strokeWidth={1.5}
                    dot={false}
                    name="Batch Size"
                    strokeDasharray="4 2"
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </div>

          {/* Category distribution */}
          <div style={card}>
            <h3 style={{ margin: '0 0 12px', fontSize: 16, fontWeight: 600 }}>
              🏷️ Category Distribution
            </h3>
            {categoryData.length === 0 ? (
              <p style={{ color: '#64748b', fontSize: 14, textAlign: 'center', padding: 40 }}>
                No data yet…
              </p>
            ) : (
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie
                    data={categoryData}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    innerRadius={50}
                    outerRadius={80}
                    paddingAngle={2}
                  >
                    {categoryData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    contentStyle={{
                      background: '#1e293b',
                      border: '1px solid rgba(255,255,255,0.1)',
                      borderRadius: 8,
                      color: '#e2e8f0',
                    }}
                  />
                  <Legend
                    wrapperStyle={{ fontSize: 11, color: '#94a3b8' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* Master / Detail */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
          {/* Master — product list */}
          <div style={{ ...card, maxHeight: 500, overflowY: 'auto' }}>
            <h3 style={{ margin: '0 0 12px', fontSize: 16, fontWeight: 600 }}>
              🛒 Generated Products
              <span style={{ fontWeight: 400, color: '#64748b', marginLeft: 8, fontSize: 13 }}>
                ({allProducts.length})
              </span>
            </h3>

            {allProducts.length === 0 ? (
              <p style={{ color: '#64748b', fontSize: 14, textAlign: 'center', padding: 40 }}>
                Products will appear here as they are generated…
              </p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                {[...allProducts].reverse().slice(0, 100).map((p, i) => (
                  <button
                    key={p.id || i}
                    onClick={() => setSelectedProduct(p)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: 12,
                      padding: '10px 14px',
                      borderRadius: 8,
                      border: selectedProduct?.id === p.id
                        ? '1px solid #6366f1'
                        : '1px solid transparent',
                      background: selectedProduct?.id === p.id
                        ? 'rgba(99,102,241,0.1)'
                        : 'rgba(255,255,255,0.03)',
                      cursor: 'pointer',
                      textAlign: 'left',
                      color: '#e2e8f0',
                      width: '100%',
                      fontSize: 14,
                      transition: 'background 0.15s',
                    }}
                  >
                    {p.imageUrl && (
                      <img
                        src={p.imageUrl}
                        alt=""
                        style={{
                          width: 40,
                          height: 40,
                          borderRadius: 6,
                          objectFit: 'cover',
                          flexShrink: 0,
                        }}
                      />
                    )}
                    <div style={{ minWidth: 0 }}>
                      <div style={{ fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {p.name}
                      </div>
                      <div style={{ fontSize: 12, color: '#64748b' }}>
                        {p.brand?.name || '—'} · {p.category?.name || '—'}
                        {p.lowestPrice != null && ` · €${p.lowestPrice.toFixed(2)}`}
                      </div>
                    </div>
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Detail panel */}
          <div style={{ ...card, maxHeight: 500, overflowY: 'auto' }}>
            <h3 style={{ margin: '0 0 12px', fontSize: 16, fontWeight: 600 }}>
              📋 Product Detail
            </h3>

            {!selectedProduct ? (
              <p style={{ color: '#64748b', fontSize: 14, textAlign: 'center', padding: 40 }}>
                Select a product from the list to see its details
              </p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                {selectedProduct.imageUrl && (
                  <img
                    src={selectedProduct.imageUrl}
                    alt={selectedProduct.name}
                    style={{
                      width: '100%',
                      maxHeight: 200,
                      objectFit: 'cover',
                      borderRadius: 8,
                    }}
                  />
                )}

                <h2 style={{ fontSize: 20, fontWeight: 600, margin: 0 }}>
                  {selectedProduct.name}
                </h2>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                  <DetailField label="Brand" value={selectedProduct.brand?.name} />
                  <DetailField label="Category" value={selectedProduct.category?.name} />
                  <DetailField label="Gender" value={selectedProduct.gender} />
                  <DetailField
                    label="Price"
                    value={
                      selectedProduct.lowestPrice != null
                        ? `€${selectedProduct.lowestPrice.toFixed(2)}`
                        : undefined
                    }
                  />
                </div>

                {selectedProduct.description && (
                  <div>
                    <span style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>
                      Description
                    </span>
                    <p style={{ fontSize: 14, color: '#cbd5e1', margin: '4px 0 0', lineHeight: 1.6 }}>
                      {selectedProduct.description}
                    </p>
                  </div>
                )}

                <div style={{ fontSize: 11, color: '#475569', marginTop: 8 }}>
                  ID: {selectedProduct.id}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

/* ─── Sub-components ───────────────────────────────────────── */

function DetailField({ label, value }: { label: string; value?: string }) {
  return (
    <div>
      <span style={{ fontSize: 12, color: '#64748b', fontWeight: 500 }}>{label}</span>
      <div style={{ fontSize: 14, fontWeight: 500, marginTop: 2 }}>{value || '—'}</div>
    </div>
  );
}
