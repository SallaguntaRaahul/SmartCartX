import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
      navigate('/products');
    } catch (err: any) {
      setError(
        err.response?.data?.message || 'Invalid email or password.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 68px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 24,
      }}
    >
      <div className="card" style={{ width: 380, padding: 32 }}>
        <h1 style={{ fontSize: 24, marginBottom: 6 }}>Welcome back</h1>
        <p style={{ color: 'var(--color-text-muted)', fontSize: 14, marginBottom: 24 }}>
          Log in to continue shopping on SmartCartX.
        </p>

        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: 16 }}>
            <label className="label">Email</label>
            <input
              className="input"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
            />
          </div>
          <div style={{ marginBottom: 20 }}>
            <label className="label">Password</label>
            <input
              className="input"
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          {error && (
            <div
              className="badge badge-danger"
              style={{ width: '100%', marginBottom: 16, padding: '10px 14px' }}
            >
              {error}
            </div>
          )}

          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
            style={{ width: '100%' }}
          >
            {loading ? <span className="spinner" /> : 'Log in'}
          </button>
        </form>

        <p style={{ fontSize: 14, marginTop: 20, textAlign: 'center', color: 'var(--color-text-muted)' }}>
          Don't have an account? <Link to="/register" style={{ color: 'var(--color-primary)', fontWeight: 600 }}>Sign up</Link>
        </p>
      </div>
    </div>
  );
}
