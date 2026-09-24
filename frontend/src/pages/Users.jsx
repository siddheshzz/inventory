import { useState } from 'react';
import { friendlyError, usersApi } from '../lib/api';
import { useAuth } from '../lib/auth';

export default function Users() {
  const { profile, isAdmin, refreshProfile } = useAuth();
  const [form, setForm] = useState({ name: profile?.name || '', email: profile?.email || '' });
  const [status, setStatus] = useState(null);
  const [busy, setBusy] = useState(false);
  const [team, setTeam] = useState(null);
  const [teamError, setTeamError] = useState(null);

  const save = async (e) => {
    e.preventDefault();
    setBusy(true);
    setStatus(null);
    try {
      // PATCH /api/v1/user/me
      await usersApi.updateMe({ name: form.name, email: form.email });
      await refreshProfile();
      setStatus({ type: 'ok', text: 'Profile updated.' });
    } catch (err) {
      setStatus({ type: 'error', text: friendlyError(err, 'Could not update your profile. Please try again.') });
    } finally {
      setBusy(false);
    }
  };

  const loadTeam = async () => {
    setTeamError(null);
    try {
      // NOTE: backend admin list route has a mapping quirk (@PathVariable on a @GetMapping
      // without a path placeholder), so this may 500 until that's fixed. Surfaced honestly here.
      const list = await usersApi.list();
      setTeam(Array.isArray(list) ? list : []);
    } catch (err) {
      setTeamError(friendlyError(err, 'Could not load the team. The admin list endpoint may need a backend fix.'));
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Team & Profile</h1>
          <p>Your identity from <span className="kbd">GET /api/v1/user/me</span>, decoded from your JWT.</p>
        </div>
      </div>

      {status && <div className={`alert ${status.type}`}>{status.text}</div>}

      <div className="grid-2">
        <div className="card">
          <h3 style={{ margin: '0 0 4px' }}>My profile</h3>
          <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}>
            <span className="kbd">PATCH /api/v1/user/me</span> · phone is immutable
          </p>
          <div className="field"><label className="label">Phone</label><input className="input" value={profile?.phoneNumber || ''} disabled /></div>
          <form onSubmit={save}>
            <div className="field"><label className="label">Name</label>
              <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} placeholder={profile?.name || 'Your name'} />
            </div>
            <div className="field"><label className="label">Email</label>
              <input className="input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder={profile?.email || 'you@example.com'} />
            </div>
            <button className="btn" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button>
          </form>
          <div className="footer-note">
            Role: <span className="kbd">{profile?.role || '—'}</span> · ID: <span className="mono">{profile?.id || '—'}</span>
          </div>
        </div>

        <div className="card">
          <h3 style={{ margin: '0 0 4px' }}>Team {isAdmin ? '(admin)' : ''}</h3>
          <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}><span className="kbd">GET /api/v1/user</span></p>
          {!isAdmin ? (
            <div className="empty"><b>Admin only</b>Sign in with the admin number to list users.</div>
          ) : team === null ? (
            <>
              <button className="btn ghost" onClick={loadTeam}>Load team</button>
              {teamError && <div className="alert error" style={{ marginTop: 12 }}>Could not load team: {teamError}</div>}
            </>
          ) : team.length === 0 ? (
            <div className="empty"><b>No users returned</b></div>
          ) : (
            <div className="table-wrap" style={{ boxShadow: 'none' }}>
              <table>
                <thead><tr><th>Name</th><th>Phone</th><th>Role</th></tr></thead>
                <tbody>
                  {team.map((u) => (
                    <tr key={u.id}><td><b>{u.name}</b></td><td className="mono">{u.phoneNumber}</td><td><span className="badge">{u.role}</span></td></tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
