import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getProducts, getProductsByCategory, searchProducts } from '../api/products';
import type { Product } from '../types';
import ProductCard from '../components/ProductCard';

const CATEGORIES = [
  'Electronics', 'Computers', 'Audio', 'Gaming', 'Sports', 'Clothing',
  'Kitchen', 'Home', 'Wearables', 'Camera', 'Health', 'Books', 'Toys',
  'Beauty', 'Automotive',
];

export default function Products() {
  const [searchParams, setSearchParams] = useSearchParams();
  const search = searchParams.get('search') || '';
  const category = searchParams.get('category') || '';
  const page = parseInt(searchParams.get('page') || '0', 10);

  const [products, setProducts] = useState<Product[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const fetchData = async () => {
      try {
        let res;
        if (search) {
          res = await searchProducts(search, page, 12);
        } else if (category) {
          res = await getProductsByCategory(category, page, 12);
        } else {
          res = await getProducts(page, 12);
        }
        setProducts(res.data.content);
        setTotalPages(res.data.totalPages);
        setTotalElements(res.data.totalElements);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [search, category, page]);

  const setCategory = (cat: string) => {
    setSearchParams(cat ? { category: cat } : {});
  };

  const setPage = (p: number) => {
    const params: Record<string, string> = { page: String(p) };
    if (search) params.search = search;
    if (category) params.category = category;
    setSearchParams(params);
  };

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60 }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: 28 }}>
          {search ? `Results for "${search}"` : category || 'All Products'}
        </h1>
        <p style={{ color: 'var(--color-text-muted)', fontSize: 14, marginTop: 4 }}>
          {totalElements.toLocaleString()} products
        </p>
      </div>

      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 28 }}>
        <button
          className="btn btn-sm"
          style={{
            background: !category ? 'var(--color-primary)' : 'var(--color-primary-soft)',
            color: !category ? 'white' : 'var(--color-primary)',
          }}
          onClick={() => setCategory('')}
        >
          All
        </button>
        {CATEGORIES.map((cat) => (
          <button
            key={cat}
            className="btn btn-sm"
            style={{
              background: category === cat ? 'var(--color-primary)' : 'var(--color-primary-soft)',
              color: category === cat ? 'white' : 'var(--color-primary)',
            }}
            onClick={() => setCategory(cat)}
          >
            {cat}
          </button>
        ))}
      </div>

      {loading ? (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
            gap: 18,
          }}
        >
          {Array.from({ length: 12 }).map((_, i) => (
            <div key={i} className="skeleton" style={{ aspectRatio: '0.78 / 1' }} />
          ))}
        </div>
      ) : products.length === 0 ? (
        <div className="card" style={{ padding: 48, textAlign: 'center', color: 'var(--color-text-muted)' }}>
          No products found. Try a different search or category.
        </div>
      ) : (
        <>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
              gap: 18,
            }}
          >
            {products.map((p) => (
              <ProductCard key={p.id} product={p} />
            ))}
          </div>

          <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginTop: 36 }}>
            <button
              className="btn btn-outline btn-sm"
              disabled={page === 0}
              onClick={() => setPage(page - 1)}
            >
              Previous
            </button>
            <span style={{ display: 'flex', alignItems: 'center', padding: '0 12px', fontSize: 14, color: 'var(--color-text-muted)' }}>
              Page {page + 1} of {totalPages.toLocaleString()}
            </span>
            <button
              className="btn btn-outline btn-sm"
              disabled={page >= totalPages - 1}
              onClick={() => setPage(page + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}
