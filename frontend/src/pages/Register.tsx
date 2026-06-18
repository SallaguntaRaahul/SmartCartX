import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const { register } = useAuth();
  const [form, setForm] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const update = (key: string) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(form);
      setSuccess(true);
    } catch (err: any) {
      setError(
        err.response?.data?.message || 'Could not create account.'
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
      <div className="card animate-scale-in" style={{ width: 400, padding: 32 }}>
        {success ? (
          <div style={{ textAlign: 'center' }}>
            <div
              style={{
                width: 56,
                height: 56,
                borderRadius: '50%',
                background: 'var(--color-success-soft)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                margin: '0 auto 20px',
                fontSize: 28,
              }}
            >
              ✓
            </div>
            <h1 style={{ fontSize: 22, marginBottom: 8 }}>Registration successful</h1>
            <p style={{ color: 'var(--color-text-muted)', fontSize: 14, marginBottom: 28 }}>
              Your account has been created. Log in to start shopping on SmartCartX.
            </p>
            <Link
              to="/login"
              className="btn btn-primary hover-lift"
              style={{ width: '100%', display: 'flex' }}
            >
              Go to login
            </Link>
          </div>
        ) : (
          <>
            <h1 style={{ fontSize: 24, marginBottom: 6 }}>Create your account</h1>
            <p style={{ color: 'var(--color-text-muted)', fontSize: 14, marginBottom: 24 }}>
              Join SmartCartX and start shopping.
            </p>

            <form onSubmit={handleSubmit}>
              <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
                <div style={{ flex: 1 }}>
                  <label className="label">First name</label>
                  <input
                    className="input"
                    required
                    value={form.firstName}
                    onChange={update('firstName')}
                  />
                </div>
                <div style={{ flex: 1 }}>
                  <label className="label">Last name</label>
                  <input
                    className="input"
                    required
                    value={form.lastName}
                    onChange={update('lastName')}
                  />
                </div>
              </div>
              <div style={{ marginBottom: 16 }}>
                <label className="label">Email</label>
                <input
                  className="input"
                  type="email"
                  required
                  value={form.email}
                  onChange={update('email')}
                  placeholder="you@example.com"
                />
              </div>
              <div style={{ marginBottom: 20 }}>
                <label className="label">Password</label>
                <input
                  className="input"
                  type="password"
                  required
                  minLength={6}
                  value={form.password}
                  onChange={update('password')}
                  placeholder="At least 6 characters"
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
                {loading ? <span className="spinner" /> : 'Create account'}
              </button>
            </form>

            <p style={{ fontSize: 14, marginTop: 20, textAlign: 'center', color: 'var(--color-text-muted)' }}>
              Already have an account? <Link to="/login" style={{ color: 'var(--color-primary)', fontWeight: 600 }}>Log in</Link>
            </p>
          </>
        )}
      </div>
    </div>
  );
}
