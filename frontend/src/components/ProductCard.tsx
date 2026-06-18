import { Link } from 'react-router-dom';
import type { Product } from '../types';
import { getCategoryImage } from '../utils/categoryImages';

export default function ProductCard({ product }: { product: Product }) {
  return (
    <Link
      to={`/products/${product.id}`}
      className="card"
      style={{
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
        transition: 'transform 0.15s ease, box-shadow 0.15s ease',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-3px)';
        e.currentTarget.style.boxShadow = 'var(--shadow-md)';
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'none';
        e.currentTarget.style.boxShadow = 'var(--shadow-sm)';
      }}
    >
      <div style={{ aspectRatio: '1 / 1', overflow: 'hidden', background: '#F1F0EE' }}>
        <img
          src={getCategoryImage(product.category, product.id)}
          alt={product.category}
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          loading="lazy"
        />
      </div>
      <div style={{ padding: 14, display: 'flex', flexDirection: 'column', gap: 6, flex: 1 }}>
        <span className="badge badge-neutral" style={{ alignSelf: 'flex-start' }}>
          {product.category}
        </span>
        <h3 style={{ fontSize: 14.5, lineHeight: 1.3, fontWeight: 600 }}>
          {product.name}
        </h3>
        <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'baseline', justifyContent: 'space-between' }}>
          <span style={{ fontFamily: 'var(--font-display)', fontWeight: 700, fontSize: 17 }}>
            ${product.price.toFixed(2)}
          </span>
          {product.stockQuantity === 0 ? (
            <span className="badge badge-danger">Out of stock</span>
          ) : product.stockQuantity < 10 ? (
            <span className="badge badge-warning">Low stock</span>
          ) : null}
        </div>
      </div>
    </Link>
  );
}
