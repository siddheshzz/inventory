import { useEffect, useState } from 'react';
import ConfirmDelete from '../components/ConfirmDelete';
import { categoriesApi, friendlyError, isForeignKeyError } from '../lib/api';
import { useAuth } from '../lib/auth';

export default function Categories() {
  const { isAdmin } = useAuth();
  const [cats, setCats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [name, setName] = useState('');
  const [editing, setEditing] = useState(null); // { id, name }
  const [busy, setBusy] = useState(false);
  const [pendingDelete, setPendingDelete] = useState(null); // category object
  const [deleting, setDeleting] = useState(false);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await categoriesApi.list();
      setCats(Array.isArray(list) ? list : []);
    } catch (e) { setError(friendlyError(e, 'Could not load categories.')); }
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  if (!isAdmin) {
    return (
      <>
        <div className="page-head"><div><h1>Categories</h1><p>Admin-only section.</p></div></div>
        <div className="card empty">
          <b>Restricted — ADMIN role required</b>
          Every <span className="kbd">/api/v1/categorie</span> endpoint is guarded by
          <span className="kbd"> @PreAuthorize("hasRole('ADMIN')")</span>.
          Sign in with the admin number to manage categories.
        </div>
      </>
    );
  }

  const create = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setBusy(true);
    try {
      await categoriesApi.create({ name: name.trim() });
      setName('');
      setNotice({ type: 'ok', text: 'Category created.' });
      await load();
    } catch (err) { setNotice({ type: 'error', text: friendlyError(err, 'Could not create the category. Please try again.') }); }
    finally { setBusy(false); }
  };

  const saveEdit = async () => {
    if (!editing?.name.trim()) return;
    setBusy(true);
    try {
      // Backend signature is updateCategory(payload) — sends name; id in path is accepted but ignored server-side.
      await categoriesApi.update(editing.id, { name: editing.name.trim() });
      setEditing(null);
      setNotice({ type: 'ok', text: 'Category updated.' });
      await load();
    } catch (err) { setNotice({ type: 'error', text: friendlyError(err, 'Could not update the category. Please try again.') }); }
    finally { setBusy(false); }
  };

  const confirmRemove = async () => {
    if (!pendingDelete) return;
    setDeleting(true);
    setNotice(null);
    try {
      await categoriesApi.remove(pendingDelete.id);
      setNotice({ type: 'ok', text: `“${pendingDelete.name}” deleted.` });
      setPendingDelete(null);
      await load();
    } catch (err) {
      const catName = pendingDelete.name;
      setPendingDelete(null);
      if (isForeignKeyError(err)) {
        setNotice({
          type: 'error',
          text: `Couldn't delete “${catName}” — products still use this category. To delete it, move those products to another category or delete them first. The category is unchanged.`,
        });
      } else {
        setNotice({ type: 'error', text: friendlyError(err, `Couldn't delete “${catName}”. Please try again.`) });
      }
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Categories</h1>
          <p>Note the backend path is singular: <span className="kbd">/api/v1/categorie</span>. All routes are ADMIN-only.</p>
        </div>
      </div>

      {notice && <div className={`alert ${notice.type}`}>{notice.text}</div>}
      {error && <div className="alert error">Could not load categories: {error}</div>}

      <div className="grid-2">
        <div className="card">
          <h3 style={{ margin: '0 0 4px' }}>New category</h3>
          <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}><span className="kbd">POST /api/v1/categorie</span></p>
          <form onSubmit={create}>
            <div className="field">
              <label className="label">Name</label>
              <input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Electronics" />
            </div>
            <button className="btn" disabled={busy || !name.trim()}>{busy ? 'Saving…' : 'Create category'}</button>
          </form>
        </div>

        <div className="card">
          <h3 style={{ margin: '0 0 4px' }}>All categories ({cats.length})</h3>
          <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}><span className="kbd">GET /api/v1/categorie</span></p>
          {loading ? 'Loading…' : cats.length === 0 ? (
            <div className="empty"><b>No categories yet</b>Create the first one to organise products.</div>
          ) : (
            <div style={{ display: 'grid', gap: 10 }}>
              {cats.map((c) => (
                <div key={c.id} style={{ border: '1px solid #eee', borderRadius: 12, padding: '10px 12px', display: 'flex', alignItems: 'center', gap: 10 }}>
                  {editing?.id === c.id ? (
                    <>
                      <input className="input" value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} />
                      <button className="btn small" onClick={saveEdit} disabled={busy}>Save</button>
                      <button className="btn ghost small" onClick={() => setEditing(null)}>Cancel</button>
                    </>
                  ) : (
                    <>
                      <div style={{ flex: 1 }}>
                        <b>{c.name}</b><br />
                        <span className="mono" style={{ color: '#6b7280' }}>{c.id}</span>
                      </div>
                      <button className="btn ghost small" onClick={() => setEditing({ id: c.id, name: c.name })}>Edit</button>
                      <button className="btn danger small" onClick={() => setPendingDelete(c)}>Delete</button>
                    </>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {pendingDelete && (
        <ConfirmDelete
          title="Delete category?"
          itemName={pendingDelete.name}
          requirements={[
            'No products use this category.',
          ]}
          blockedHint="If products still use it, deletion is blocked to protect them. Move those products to another category (edit each product) or delete them first — then try again."
          confirmLabel="Delete category"
          busy={deleting}
          onCancel={() => setPendingDelete(null)}
          onConfirm={confirmRemove}
        />
      )}
    </>
  );
}
