import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getProductById } from '../api/products';
import { addToCart } from '../api/cart';
import type { Product } from '../types';
import { getCategoryImage } from '../utils/categoryImages';
import { useAuth } from '../context/AuthContext';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [product, setProduct] = useState<Product | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [adding, setAdding] = useState(false);
  const [added, setAdded] = useState(false);

  useEffect(() => {
    if (!id) return;
    setLoading(true);
    getProductById(Number(id))
      .then((res) => setProduct(res.data))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (!product) return;
    setAdding(true);
    try {
      await addToCart(product.id, quantity);
      setAdded(true);
      setTimeout(() => setAdded(false), 2000);
    } finally {
      setAdding(false);
    }
  };

  if (loading) {
    return (
      <div className="container" style={{ paddingTop: 32 }}>
        <div className="skeleton" style={{ height: 400, borderRadius: 16 }} />
      </div>
    );
  }

  if (!product) {
    return (
      <div className="container" style={{ paddingTop: 60, textAlign: 'center' }}>
        <h2>Product not found</h2>
      </div>
    );
  }

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: 48,
          alignItems: 'start',
        }}
      >
        <div
          className="card"
          style={{ aspectRatio: '1 / 1', overflow: 'hidden' }}
        >
          <img
            src={getCategoryImage(product.category, product.id)}
            alt={product.category}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        </div>

        <div>
          <span className="badge badge-neutral" style={{ marginBottom: 12 }}>
            {product.category}
          </span>
          <h1 style={{ fontSize: 28, marginBottom: 12 }}>{product.name}</h1>
          <p style={{ color: 'var(--color-text-muted)', fontSize: 15, lineHeight: 1.6, marginBottom: 20 }}>
            {product.description}
          </p>

          <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 32, marginBottom: 8 }}>
            ${product.price.toFixed(2)}
          </div>

          <div style={{ marginBottom: 24 }}>
            {product.stockQuantity === 0 ? (
              <span className="badge badge-danger">Out of stock</span>
            ) : product.stockQuantity < 10 ? (
              <span className="badge badge-warning">Only {product.stockQuantity} left</span>
            ) : (
              <span className="badge badge-success">In stock</span>
            )}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginBottom: 20 }}>
            <label className="label" style={{ marginBottom: 0 }}>Quantity</label>
            <div style={{ display: 'flex', alignItems: 'center', border: '1.5px solid var(--color-border)', borderRadius: 10 }}>
              <button
                className="btn btn-sm"
                style={{ background: 'transparent' }}
                onClick={() => setQuantity((q) => Math.max(1, q - 1))}
              >
                −
              </button>
              <span style={{ width: 36, textAlign: 'center', fontWeight: 600 }}>{quantity}</span>
              <button
                className="btn btn-sm"
                style={{ background: 'transparent' }}
                onClick={() => setQuantity((q) => q + 1)}
              >
                +
              </button>
            </div>
          </div>

          <button
            className="btn btn-primary"
            style={{ width: '100%', padding: '14px 20px', fontSize: 15 }}
            disabled={product.stockQuantity === 0 || adding}
            onClick={handleAddToCart}
          >
            {adding ? (
              <span className="spinner" style={{ borderTopColor: 'white', borderColor: 'rgba(255,255,255,0.3)' }} />
            ) : added ? (
              'Added to cart ✓'
            ) : (
              'Add to cart'
            )}
          </button>
        </div>
      </div>
    </div>
  );
}
