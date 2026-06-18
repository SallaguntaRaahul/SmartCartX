import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getOrderById, cancelOrder } from '../api/orders';
import { getOrderSummary, getOrderAnomaly } from '../api/ai';
import type { Order } from '../types';

const statusBadge = (status: string) => {
  if (status === 'CANCELLED') return 'badge-danger';
  if (status === 'COMPLETED') return 'badge-success';
  return 'badge-warning';
};

function parseAnomaly(raw: string): { isAnomalous: boolean; riskLevel: string; reason: string } | null {
  try {
    const match = raw.match(/\{[\s\S]*\}/);
    if (!match) return null;
    return JSON.parse(match[0]);
  } catch {
    return null;
  }
}

export default function OrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);

  const [summary, setSummary] = useState<string | null>(null);
  const [summaryLoading, setSummaryLoading] = useState(false);

  const [anomaly, setAnomaly] = useState<string | null>(null);
  const [anomalyLoading, setAnomalyLoading] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getOrderById(Number(id))
      .then((res) => setOrder(res.data))
      .finally(() => setLoading(false));
  }, [id]);

  const handleCancel = async () => {
    if (!order) return;
    setCancelling(true);
    try {
      const res = await cancelOrder(order.id);
      setOrder(res.data);
    } finally {
      setCancelling(false);
    }
  };

  const loadSummary = async () => {
    if (!id) return;
    setSummaryLoading(true);
    try {
      const res = await getOrderSummary(Number(id));
      setSummary(res.data.summary);
    } catch {
      setSummary('Could not generate summary right now.');
    } finally {
      setSummaryLoading(false);
    }
  };

  const loadAnomaly = async () => {
    if (!id) return;
    setAnomalyLoading(true);
    try {
      const res = await getOrderAnomaly(Number(id));
      setAnomaly(res.data.analysis);
    } catch {
      setAnomaly('Could not run anomaly check right now.');
    } finally {
      setAnomalyLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ paddingTop: 32 }}>
        <div className="skeleton" style={{ height: 300, borderRadius: 16 }} />
      </div>
    );
  }

  if (!order) {
    return (
      <div className="container" style={{ paddingTop: 60, textAlign: 'center' }}>
        <h2>Order not found</h2>
      </div>
    );
  }

  const parsedAnomaly = anomaly ? parseAnomaly(anomaly) : null;

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60, maxWidth: 800 }}>
      <button
        className="btn btn-outline btn-sm"
        style={{ marginBottom: 20 }}
        onClick={() => navigate('/orders')}
      >
        ← Back to orders
      </button>

      <div className="card" style={{ padding: 24, marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
          <div>
            <h1 style={{ fontSize: 24 }}>Order #{order.id}</h1>
            <p style={{ fontSize: 13, color: 'var(--color-text-muted)', marginTop: 4 }}>
              Placed {new Date(order.createdAt).toLocaleString()}
            </p>
          </div>
          <span className={`badge ${statusBadge(order.status)}`}>{order.status}</span>
        </div>

        {order.items.map((item) => (
          <div
            key={item.id}
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              padding: '10px 0',
              borderTop: '1px solid var(--color-border)',
              fontSize: 14,
            }}
          >
            <span>{item.productName} × {item.quantity}</span>
            <span style={{ fontWeight: 600 }}>${item.subtotal.toFixed(2)}</span>
          </div>
        ))}

        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            paddingTop: 14,
            marginTop: 4,
            borderTop: '1.5px solid var(--color-border)',
            fontWeight: 700,
            fontSize: 17,
          }}
        >
          <span>Total</span>
          <span>${order.totalAmount.toFixed(2)}</span>
        </div>

        {order.status === 'PENDING' && (
          <button
            className="btn btn-danger btn-sm"
            style={{ marginTop: 16 }}
            disabled={cancelling}
            onClick={handleCancel}
          >
            {cancelling ? 'Cancelling...' : 'Cancel order'}
          </button>
        )}
      </div>

      <div className="card" style={{ padding: 24, marginBottom: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3 style={{ fontSize: 16 }}>✨ AI order summary</h3>
          {!summary && (
            <button className="btn btn-primary btn-sm" disabled={summaryLoading} onClick={loadSummary}>
              {summaryLoading ? <span className="spinner" style={{ borderTopColor: 'white' }} /> : 'Generate'}
            </button>
          )}
        </div>
        {summary && (
          <p style={{ fontSize: 14, lineHeight: 1.6, color: 'var(--color-text)', whiteSpace: 'pre-line' }}>
            {summary}
          </p>
        )}
      </div>

      <div className="card" style={{ padding: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
          <h3 style={{ fontSize: 16 }}>🛡️ AI anomaly check</h3>
          {!anomaly && (
            <button className="btn btn-primary btn-sm" disabled={anomalyLoading} onClick={loadAnomaly}>
              {anomalyLoading ? <span className="spinner" style={{ borderTopColor: 'white' }} /> : 'Run check'}
            </button>
          )}
        </div>
        {anomaly && parsedAnomaly && (
          <div>
            <span
              className={`badge ${
                parsedAnomaly.riskLevel === 'HIGH'
                  ? 'badge-danger'
                  : parsedAnomaly.riskLevel === 'MEDIUM'
                  ? 'badge-warning'
                  : 'badge-success'
              }`}
              style={{ marginBottom: 10 }}
            >
              {parsedAnomaly.riskLevel} risk
            </span>
            <p style={{ fontSize: 14, lineHeight: 1.6, marginTop: 10 }}>{parsedAnomaly.reason}</p>
          </div>
        )}
        {anomaly && !parsedAnomaly && (
          <p style={{ fontSize: 14, lineHeight: 1.6, whiteSpace: 'pre-line' }}>{anomaly}</p>
        )}
      </div>
    </div>
  );
}
