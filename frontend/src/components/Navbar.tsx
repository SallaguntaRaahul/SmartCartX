import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState, useRef, useEffect } from 'react';
import Logo from './Logo';

const CATEGORIES = [
  'Electronics', 'Computers', 'Audio', 'Gaming', 'Sports', 'Clothing',
  'Kitchen', 'Home', 'Wearables', 'Camera', 'Health', 'Books', 'Toys',
  'Beauty', 'Automotive',
];

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [showCategories, setShowCategories] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setShowCategories(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/products?search=${encodeURIComponent(query.trim())}`);
    }
  };

  return (
    <header
      style={{
        borderBottom: '1px solid var(--color-border)',
        background: 'var(--color-surface)',
        position: 'sticky',
        top: 0,
        zIndex: 50,
      }}
    >
      <div
        className="container"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 24,
          height: 68,
        }}
      >
        <Link to="/" style={{ display: 'flex' }}>
          <Logo />
        </Link>

        <div ref={dropdownRef} style={{ position: 'relative' }}>
          <button
            className="btn btn-outline btn-sm"
            onClick={() => setShowCategories((s) => !s)}
            style={{ display: 'flex', alignItems: 'center', gap: 6 }}
          >
            Categories
            <span style={{ fontSize: 10, transform: showCategories ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s ease' }}>
              ▼
            </span>
          </button>
          {showCategories && (
            <div
              className="card dropdown-enter"
              style={{
                position: 'absolute',
                top: 'calc(100% + 8px)',
                left: 0,
                width: 220,
                padding: 8,
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: 2,
                zIndex: 100,
              }}
            >
              {CATEGORIES.map((cat) => (
                <Link
                  key={cat}
                  to={`/products?category=${cat}`}
                  onClick={() => setShowCategories(false)}
                  style={{
                    fontSize: 13,
                    padding: '8px 10px',
                    borderRadius: 8,
                    transition: 'background 0.15s ease',
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.background = 'var(--color-primary-soft)')}
                  onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                >
                  {cat}
                </Link>
              ))}
            </div>
          )}
        </div>

        <form onSubmit={handleSearch} style={{ flex: 1, maxWidth: 420 }}>
          <input
            className="input"
            placeholder="Search products..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </form>

        <nav style={{ display: 'flex', alignItems: 'center', gap: 18, marginLeft: 'auto' }}>
          <Link to="/products" style={{ fontSize: 14, fontWeight: 600 }}>
            Shop
          </Link>
          {isAuthenticated && (
            <>
              <Link to="/cart" style={{ fontSize: 14, fontWeight: 600 }}>
                Cart
              </Link>
              <Link to="/orders" style={{ fontSize: 14, fontWeight: 600 }}>
                Orders
              </Link>
              <Link to="/recommendations" style={{ fontSize: 14, fontWeight: 600 }}>
                For You
              </Link>
              <Link to="/profile" style={{ fontSize: 14, fontWeight: 600 }}>
                {user?.firstName}
              </Link>
              <button className="btn btn-outline btn-sm" onClick={handleLogout}>
                Log out
              </button>
            </>
          )}
          {!isAuthenticated && (
            <>
              <Link to="/login" style={{ fontSize: 14, fontWeight: 600 }}>
                Log in
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm hover-lift">
                Sign up
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
