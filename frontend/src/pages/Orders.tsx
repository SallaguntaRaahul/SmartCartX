import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getMyOrders, cancelOrder } from '../api/orders';
import type { Order } from '../types';

const statusBadge = (status: string) => {
  if (status === 'CANCELLED') return 'badge-danger';
  if (status === 'COMPLETED') return 'badge-success';
  return 'badge-warning';
};

export default function Orders() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState<number | null>(null);

  const fetchOrders = () => {
    setLoading(true);
    getMyOrders(0, 50)
      .then((res) => setOrders(res.data.content))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleCancel = async (e: React.MouseEvent, orderId: number) => {
    e.stopPropagation();
    setCancellingId(orderId);
    try {
      await cancelOrder(orderId);
      fetchOrders();
    } finally {
      setCancellingId(null);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ paddingTop: 32 }}>
        <div className="skeleton" style={{ height: 300, borderRadius: 16 }} />
      </div>
    );
  }

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60, maxWidth: 800 }}>
      <h1 style={{ fontSize: 28, marginBottom: 24 }}>Your orders</h1>

      {orders.length === 0 ? (
        <div className="card" style={{ padding: 48, textAlign: 'center' }}>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: 16 }}>
            You haven't placed any orders yet.
          </p>
          <button className="btn btn-primary" onClick={() => navigate('/products')}>
            Browse products
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {orders.map((order) => (
            <div
              key={order.id}
              className="card"
              style={{ padding: 18, cursor: 'pointer' }}
              onClick={() => navigate(`/orders/${order.id}`)}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                  <span style={{ fontWeight: 700 }}>Order #{order.id}</span>
                  <span className={`badge ${statusBadge(order.status)}`}>{order.status}</span>
                </div>
                <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 17 }}>
                  ${order.totalAmount.toFixed(2)}
                </span>
              </div>
              <p style={{ fontSize: 13, color: 'var(--color-text-muted)', marginBottom: 10 }}>
                {order.items.length} item{order.items.length !== 1 ? 's' : ''} ·{' '}
                {new Date(order.createdAt).toLocaleDateString(undefined, {
                  year: 'numeric', month: 'short', day: 'numeric',
                })}
              </p>
              {order.status === 'PENDING' && (
                <button
                  className="btn btn-danger btn-sm"
                  disabled={cancellingId === order.id}
                  onClick={(e) => handleCancel(e, order.id)}
                >
                  {cancellingId === order.id ? 'Cancelling...' : 'Cancel order'}
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
