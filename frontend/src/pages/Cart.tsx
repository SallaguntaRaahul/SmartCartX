import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCart, updateCartItem, removeCartItem } from '../api/cart';
import { placeOrder } from '../api/orders';
import type { Cart as CartType } from '../types';
import { getCategoryImage } from '../utils/categoryImages';

export default function Cart() {
  const navigate = useNavigate();
  const [cart, setCart] = useState<CartType | null>(null);
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [updatingId, setUpdatingId] = useState<number | null>(null);

  const fetchCart = () => {
    setLoading(true);
    getCart()
      .then((res) => setCart(res.data))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const handleUpdate = async (itemId: number, quantity: number) => {
    if (quantity < 1) return;
    setUpdatingId(itemId);
    try {
      const res = await updateCartItem(itemId, quantity);
      setCart(res.data);
    } finally {
      setUpdatingId(null);
    }
  };

  const handleRemove = async (itemId: number) => {
    setUpdatingId(itemId);
    try {
      const res = await removeCartItem(itemId);
      setCart(res.data);
    } finally {
      setUpdatingId(null);
    }
  };

  const handlePlaceOrder = async () => {
    setPlacing(true);
    try {
      const res = await placeOrder();
      navigate(`/orders/${res.data.id}`);
    } finally {
      setPlacing(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ paddingTop: 32 }}>
        <div className="skeleton" style={{ height: 300, borderRadius: 16 }} />
      </div>
    );
  }

  const isEmpty = !cart || cart.items.length === 0;

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60, maxWidth: 800 }}>
      <h1 style={{ fontSize: 28, marginBottom: 24 }}>Your cart</h1>

      {isEmpty ? (
        <div className="card" style={{ padding: 48, textAlign: 'center' }}>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: 16 }}>
            Your cart is empty.
          </p>
          <button className="btn btn-primary" onClick={() => navigate('/products')}>
            Browse products
          </button>
        </div>
      ) : (
        <>
          <div className="card" style={{ overflow: 'hidden' }}>
            {cart!.items.map((item, idx) => (
              <div
                key={item.id}
                style={{
                  display: 'flex',
                  gap: 16,
                  padding: 16,
                  borderBottom:
                    idx < cart!.items.length - 1 ? '1px solid var(--color-border)' : 'none',
                  opacity: updatingId === item.id ? 0.5 : 1,
                  transition: 'opacity 0.15s ease',
                }}
              >
                <img
                  src={getCategoryImage(item.category, item.productId)}
                  alt=""
                  style={{ width: 64, height: 64, borderRadius: 10, objectFit: 'cover' }}
                />
                <div style={{ flex: 1 }}>
                  <h3 style={{ fontSize: 15, marginBottom: 4 }}>{item.productName}</h3>
                  <span style={{ color: 'var(--color-text-muted)', fontSize: 13 }}>
                    ${item.productPrice.toFixed(2)} each
                  </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <div style={{ display: 'flex', alignItems: 'center', border: '1.5px solid var(--color-border)', borderRadius: 10 }}>
                    <button
                      className="btn btn-sm"
                      style={{ background: 'transparent' }}
                      disabled={updatingId === item.id}
                      onClick={() => handleUpdate(item.id, item.quantity - 1)}
                    >
                      −
                    </button>
                    <span style={{ width: 32, textAlign: 'center', fontWeight: 600 }}>
                      {item.quantity}
                    </span>
                    <button
                      className="btn btn-sm"
                      style={{ background: 'transparent' }}
                      disabled={updatingId === item.id}
                      onClick={() => handleUpdate(item.id, item.quantity + 1)}
                    >
                      +
                    </button>
                  </div>
                  <span style={{ fontWeight: 700, width: 80, textAlign: 'right' }}>
                    ${item.subtotal.toFixed(2)}
                  </span>
                  <button
                    className="btn btn-danger btn-sm"
                    disabled={updatingId === item.id}
                    onClick={() => handleRemove(item.id)}
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div
            className="card"
            style={{
              marginTop: 20,
              padding: 20,
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div>
              <div style={{ fontSize: 13, color: 'var(--color-text-muted)' }}>
                {cart!.totalItems} item{cart!.totalItems !== 1 ? 's' : ''}
              </div>
              <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 24 }}>
                ${cart!.totalPrice.toFixed(2)}
              </div>
            </div>
            <button
              className="btn btn-accent"
              style={{ padding: '14px 28px', fontSize: 15 }}
              disabled={placing}
              onClick={handlePlaceOrder}
            >
              {placing ? <span className="spinner" style={{ borderTopColor: 'white' }} /> : 'Place order'}
            </button>
          </div>
        </>
      )}
    </div>
  );
}
