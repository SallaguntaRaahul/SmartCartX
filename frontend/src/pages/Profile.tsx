import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { updateMe } from '../api/user';

export default function Profile() {
  const { user } = useAuth();
  const [firstName, setFirstName] = useState(user?.firstName || '');
  const [lastName, setLastName] = useState(user?.lastName || '');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setSaved(false);
    try {
      await updateMe({ firstName, lastName });
      setSaved(true);
      setTimeout(() => setSaved(false), 2500);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="container" style={{ paddingTop: 32, paddingBottom: 60, maxWidth: 500 }}>
      <h1 style={{ fontSize: 28, marginBottom: 24 }}>Your profile</h1>

      <div className="card" style={{ padding: 28 }}>
        <form onSubmit={handleSave}>
          <div style={{ marginBottom: 16 }}>
            <label className="label">Email</label>
            <input className="input" value={user?.email || ''} disabled style={{ background: '#F5F4F2', color: 'var(--color-text-muted)' }} />
          </div>
          <div style={{ display: 'flex', gap: 12, marginBottom: 20 }}>
            <div style={{ flex: 1 }}>
              <label className="label">First name</label>
              <input
                className="input"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                required
              />
            </div>
            <div style={{ flex: 1 }}>
              <label className="label">Last name</label>
              <input
                className="input"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                required
              />
            </div>
          </div>

          {saved && (
            <div className="badge badge-success" style={{ width: '100%', marginBottom: 16, padding: '10px 14px' }}>
              Profile updated ✓
            </div>
          )}

          <button type="submit" className="btn btn-primary" disabled={saving} style={{ width: '100%' }}>
            {saving ? <span className="spinner" style={{ borderTopColor: 'white' }} /> : 'Save changes'}
          </button>
        </form>
      </div>
    </div>
  );
}
