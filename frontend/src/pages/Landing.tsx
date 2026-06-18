import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getProducts } from '../api/products';
import type { Product } from '../types';
import ProductCard from '../components/ProductCard';
import HeroCarousel from '../components/HeroCarousel';
import { getCategoryImage } from '../utils/categoryImages';

const FEATURED_CATEGORIES = [
  'Electronics', 'Sports', 'Kitchen', 'Gaming', 'Beauty', 'Camera',
];

export default function Landing() {
  const [featured, setFeatured] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getProducts(0, 8, 'id')
      .then((res) => setFeatured(res.data.content))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <HeroCarousel />

      {/* Featured categories */}
      <section className="container" style={{ padding: '56px 24px' }}>
        <h2 className="animate-fade-up" style={{ fontSize: 22, marginBottom: 20 }}>
          Shop by category
        </h2>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(160px, 1fr))',
            gap: 16,
          }}
        >
          {FEATURED_CATEGORIES.map((cat, i) => (
            <Link
              key={cat}
              to={`/products?category=${cat}`}
              className={`card hover-lift animate-scale-in stagger-${Math.min(i + 1, 6)}`}
              style={{
                overflow: 'hidden',
                position: 'relative',
                aspectRatio: '1.1 / 1',
                display: 'block',
              }}
            >
              <img
                src={getCategoryImage(cat, 0)}
                alt={cat}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
              <div
                style={{
                  position: 'absolute',
                  inset: 0,
                  background: 'linear-gradient(to top, rgba(0,0,0,0.55), transparent 60%)',
                  display: 'flex',
                  alignItems: 'flex-end',
                  padding: 14,
                }}
              >
                <span style={{ color: 'white', fontWeight: 700, fontSize: 15 }}>{cat}</span>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured products */}
      <section className="container" style={{ padding: '0 24px 64px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 20 }}>
          <h2 className="animate-fade-up" style={{ fontSize: 22 }}>Popular right now</h2>
          <Link to="/products" style={{ fontSize: 14, fontWeight: 600, color: 'var(--color-primary)' }}>
            View all →
          </Link>
        </div>
        {loading ? (
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
            {featured.map((p, i) => (
              <div key={p.id} className={`animate-scale-in stagger-${Math.min(i + 1, 6)}`}>
                <ProductCard product={p} />
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
