import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getRecommendations } from '../api/ai';
import { getMyOrders } from '../api/orders';
import { getProducts } from '../api/products';
import type { Product, Order, AIRecommendationResponse } from '../types';
import ProductCard from '../components/ProductCard';

export default function Home() {
  const { user } = useAuth();

  const [recs, setRecs] = useState<AIRecommendationResponse | null>(null);
  const [recsLoading, setRecsLoading] = useState(true);

  const [recentOrders, setRecentOrders] = useState<Order[]>([]);
  const [trending, setTrending] = useState<Product[]>([]);
  const [trendingLoading, setTrendingLoading] = useState(true);

  useEffect(() => {
    getRecommendations()
      .then((res) => setRecs(res.data))
      .catch(() => setRecs(null))
      .finally(() => setRecsLoading(false));

    getMyOrders(0, 3).then((res) => setRecentOrders(res.data.content));

    getProducts(0, 8, 'id')
      .then((res) => setTrending(res.data.content))
      .finally(() => setTrendingLoading(false));
  }, []);

  const contextLabel = (source?: string) => {
    if (!source || source === 'none') return null;
    if (source === 'cart') return 'Based on your cart';
    if (source.startsWith('recent order')) return `Based on your ${source}`;
    return null;
  };

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 64 }}>
      <div className="animate-fade-up" style={{ marginBottom: 32 }}>
        <h1 style={{ fontSize: 28 }}>Welcome back, {user?.firstName} 👋</h1>
        <p style={{ color: 'var(--color-text-muted)', fontSize: 14, marginTop: 4 }}>
          Here's what's happening with your account today.
        </p>
      </div>

      {/* Quick links */}
      <div
        className="animate-fade-up stagger-1"
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: 16,
          marginBottom: 36,
        }}
      >
        <Link to="/cart" className="card hover-lift" style={{ padding: 20 }}>
          <div style={{ fontSize: 13, color: 'var(--color-text-muted)', marginBottom: 4 }}>Your cart</div>
          <div style={{ fontWeight: 700, fontSize: 16 }}>View &amp; checkout →</div>
        </Link>
        <Link to="/orders" className="card hover-lift" style={{ padding: 20 }}>
          <div style={{ fontSize: 13, color: 'var(--color-text-muted)', marginBottom: 4 }}>Orders</div>
          <div style={{ fontWeight: 700, fontSize: 16 }}>
            {recentOrders.length > 0 ? `${recentOrders.length} recent` : 'No orders yet'} →
          </div>
        </Link>
        <Link to="/products" className="card hover-lift" style={{ padding: 20 }}>
          <div style={{ fontSize: 13, color: 'var(--color-text-muted)', marginBottom: 4 }}>Catalog</div>
          <div style={{ fontWeight: 700, fontSize: 16 }}>2,000+ products →</div>
        </Link>
      </div>

      {/* AI Recommendations */}
      <section className="animate-fade-up stagger-2" style={{ marginBottom: 40 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 14 }}>
          <h2 style={{ fontSize: 20 }}>✨ Picked for you</h2>
          <Link to="/recommendations" style={{ fontSize: 13, fontWeight: 600, color: 'var(--color-primary)' }}>
            See details →
          </Link>
        </div>

        {recsLoading ? (
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
        ) : !recs || recs.contextSource === 'none' || recs.products.length === 0 ? (
          <div className="card" style={{ padding: 28, textAlign: 'center' }}>
            <p style={{ color: 'var(--color-text-muted)', marginBottom: 14, fontSize: 14 }}>
              Add something to your cart or place an order to unlock personalized picks.
            </p>
            <Link to="/products" className="btn btn-primary btn-sm">
              Start shopping
            </Link>
          </div>
        ) : (
          <>
            <div className="card" style={{ padding: 20, marginBottom: 18 }}>
              {contextLabel(recs.contextSource) && (
                <span className="badge badge-neutral" style={{ marginBottom: 10 }}>
                  {contextLabel(recs.contextSource)}
                </span>
              )}
              <p style={{ fontSize: 14, lineHeight: 1.6 }}>{recs.reason}</p>
            </div>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
                gap: 18,
              }}
            >
              {recs.products.map((p) => (
                <ProductCard key={p.id} product={p} />
              ))}
            </div>
          </>
        )}
      </section>

      {/* Trending products */}
      <section className="animate-fade-up stagger-3">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 16 }}>
          <h2 style={{ fontSize: 20 }}>Trending products</h2>
          <Link to="/products" style={{ fontSize: 13, fontWeight: 600, color: 'var(--color-primary)' }}>
            View all →
          </Link>
        </div>
        {trendingLoading ? (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
              gap: 18,
            }}
          >
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="skeleton" style={{ aspectRatio: '0.78 / 1' }} />
            ))}
          </div>
        ) : (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
              gap: 18,
            }}
          >
            {trending.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
