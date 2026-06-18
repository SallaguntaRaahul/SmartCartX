import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getRecommendations } from '../api/ai';
import type { AIRecommendationResponse } from '../types';
import ProductCard from '../components/ProductCard';

export default function Recommendations() {
  const [data, setData] = useState<AIRecommendationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);
    getRecommendations()
      .then((res) => setData(res.data))
      .catch(() => setError('Could not load recommendations right now.'))
      .finally(() => setLoading(false));
  }, []);

  const contextLabel = (source?: string) => {
    if (!source || source === 'none') return null;
    if (source === 'cart') return 'Based on items in your cart';
    if (source.startsWith('recent order')) return `Based on your ${source}`;
    return null;
  };

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <h1 className="animate-fade-up" style={{ fontSize: 28, marginBottom: 6 }}>
        ✨ For you
      </h1>
      <p style={{ color: 'var(--color-text-muted)', fontSize: 14, marginBottom: 24 }}>
        AI-generated product suggestions based on your shopping activity.
      </p>

      {loading ? (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
            gap: 18,
          }}
        >
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="skeleton" style={{ aspectRatio: '0.78 / 1' }} />
          ))}
        </div>
      ) : error ? (
        <div className="card" style={{ padding: 32, textAlign: 'center', color: 'var(--color-text-muted)' }}>
          {error}
        </div>
      ) : !data || data.contextSource === 'none' || data.products.length === 0 ? (
        <div className="card" style={{ padding: 40, textAlign: 'center' }}>
          <p style={{ color: 'var(--color-text-muted)', marginBottom: 16 }}>
            No cart items or order history yet — add something to your cart
            or place an order to get personalized picks.
          </p>
          <Link to="/products" className="btn btn-primary">
            Browse products
          </Link>
        </div>
      ) : (
        <>
          <div className="card animate-fade-up" style={{ padding: 24, marginBottom: 28 }}>
            {contextLabel(data.contextSource) && (
              <span className="badge badge-neutral" style={{ marginBottom: 12 }}>
                {contextLabel(data.contextSource)}
              </span>
            )}
            <p style={{ fontSize: 14.5, lineHeight: 1.7 }}>{data.reason}</p>
          </div>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
              gap: 18,
            }}
          >
            {data.products.map((p, i) => (
              <div key={p.id} className={`animate-scale-in stagger-${Math.min(i + 1, 6)}`}>
                <ProductCard product={p} />
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
