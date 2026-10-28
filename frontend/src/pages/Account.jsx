import { useEffect, useState } from 'react';
import { addressesApi, usersApi, friendlyError } from '../lib/api';
import { useAuth } from '../lib/auth';
import Modal from '../components/Modal';

const blankAddress = { addressLine1: '', addressLine2: '', city: '', state: '', postalCode: '', country: 'India', isDefault: false };
const addressFields = [['addressLine1', 'Street address', 150], ['addressLine2', 'Apartment, suite or landmark (optional)', 150], ['city', 'City', 100], ['state', 'State / region', 100], ['postalCode', 'Postal code', 20], ['country', 'Country', 100]];

export default function Account() {
  const { profile, refreshProfile } = useAuth();
  const [form, setForm] = useState({ name: '', email: '' });
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState(null);
  const [editing, setEditing] = useState(null);
  const [removing, setRemoving] = useState(null);
  const [busy, setBusy] = useState(false);
  useEffect(() => { if (profile) setForm({ name: profile.name || '', email: profile.email || '' }); }, [profile]);
  const load = async () => { try { setAddresses(await addressesApi.list()); } catch (e) { setNotice({ type: 'error', text: friendlyError(e, 'Could not load addresses.') }); } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);
  const saveProfile = async e => {
    e.preventDefault(); setBusy(true); setNotice(null);
    try { await usersApi.updateMe({ name: form.name.trim(), email: form.email.trim() }); await refreshProfile(); setNotice({ type: 'ok', text: 'Your profile is up to date.' }); }
    catch (e) { setNotice({ type: 'error', text: friendlyError(e, 'Could not save your profile.') }); } finally { setBusy(false); }
  };
  const saveAddress = async e => {
    e.preventDefault(); setBusy(true); setNotice(null);
    try { if (editing.id) await addressesApi.update(editing.id, editing); else await addressesApi.create(editing); setEditing(null); await load(); setNotice({ type: 'ok', text: 'Address saved.' }); }
    catch (e) { setNotice({ type: 'error', text: friendlyError(e, 'Could not save this address.') }); } finally { setBusy(false); }
  };
  const remove = async () => {
    setBusy(true);
    try { await addressesApi.remove(removing.id); setRemoving(null); await load(); setNotice({ type: 'ok', text: 'Address removed.' }); }
    catch (e) { setNotice({ type: 'error', text: friendlyError(e, 'Could not remove this address.') }); } finally { setBusy(false); }
  };
  return <>
    <div className="page-head"><div><span className="section-kicker">YOUR SPACE</span><h1>My account</h1><p>Your details, just the way you want them.</p></div></div>
    {notice && <div role="status" className={`alert ${notice.type}`}>{notice.text}</div>}
    <div className="account-grid"><section className="card"><h2>Personal details</h2><p className="muted">Your name appears on your orders.</p>
      <form onSubmit={saveProfile}><label className="field-label">Full name<input className="input" required maxLength={100} value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} /></label><label className="field-label">Email<input className="input" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} /></label><label className="field-label">Sign-in phone<input className="input" disabled value={profile?.phoneNumber || ''} /></label><p className="muted">Your phone number is your sign-in identity.</p><button className="btn" disabled={busy || !profile}>{busy ? 'Saving…' : 'Save details'}</button></form>
    </section><section><div className="section-heading"><div><h2>Address book</h2><p className="muted">Save and manage your personal addresses.</p></div><button className="btn" onClick={() => setEditing({ ...blankAddress })}>+ Add address</button></div>
      {loading ? <div className="card">Loading addresses…</div> : !addresses.length ? <div className="card empty"><b>A place to start</b>Add your first address to keep your details together.</div> : <div className="address-list">{addresses.map(a => <article className="card" key={a.id}><div className="section-heading"><b>{a.city}</b>{a.isDefault && <span className="badge green">Default address</span>}</div><p>{a.addressLine1}<br />{a.addressLine2 && <>{a.addressLine2}<br /></>}{a.city}, {a.state} {a.postalCode}<br />{a.country}</p><div className="button-row"><button className="btn ghost small" onClick={() => setEditing({ ...a })}>Edit address</button><button className="btn ghost small" onClick={() => setRemoving(a)}>Remove</button></div></article>)}</div>}
    </section></div>
    {editing && <Modal title={editing.id ? 'Edit address' : 'New address'} busy={busy} onClose={() => setEditing(null)}><form onSubmit={saveAddress}><div className="form-grid">{addressFields.map(([key, label, max]) => <label className="field-label" key={key}>{label}<input className="input" maxLength={max} required={key !== 'addressLine2'} value={editing[key] || ''} onChange={e => setEditing({ ...editing, [key]: e.target.value })} /></label>)}</div><label className="check-label"><input type="checkbox" checked={!!editing.isDefault} onChange={e => setEditing({ ...editing, isDefault: e.target.checked })} /> Use as my default address</label>{notice?.type === 'error' && <p role="alert">{notice.text}</p>}<button className="btn" disabled={busy}>Save address</button></form></Modal>}
    {removing && <Modal title="Remove address?" busy={busy} onClose={() => setRemoving(null)}><p>{removing.addressLine1}, {removing.city}</p><button className="btn danger" disabled={busy} onClick={remove}>Remove address</button></Modal>}
  </>;
}
