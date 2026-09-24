import { useEffect, useMemo, useState } from 'react';
import ConfirmDelete from '../components/ConfirmDelete';
import { categoriesApi, friendlyError, isForeignKeyError, productsApi } from '../lib/api';
import { useAuth } from '../lib/auth';

const emptyForm = { name: '', description: '', price: '', categoryId: '' };

export default function Products() {
  const { isAdmin } = useAuth();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notice, setNotice] = useState(null);
  const [query, setQuery] = useState('');
  const [catFilter, setCatFilter] = useState('all');
  const [stockFilter, setStockFilter] = useState('all');
  const [modal, setModal] = useState(null); // { mode: 'create'|'edit', value }
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [pendingDelete, setPendingDelete] = useState(null); // product object
  const [deleting, setDeleting] = useState(false);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await productsApi.list();
      setProducts(Array.isArray(list) ? list : []);
    } catch (e) { setError(friendlyError(e, 'Could not load products.')); }
    try {
      const cats = await categoriesApi.list();
      setCategories(Array.isArray(cats) ? cats : []);
    } catch { /* non-admin can't list categories — fine */ }
    setLoading(false);
  };

  useEffect(() => { load(); }, []);

  const catName = (id) => categories.find((c) => c.id === id)?.name || (id ? `${String(id).slice(0, 8)}…` : '—');

  const filtered = useMemo(() => {
    return products.filter((p) => {
      const q = query.toLowerCase();
      const matchesQ = !q || p.name?.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q);
      const matchesC = catFilter === 'all' || p.categoryId === catFilter;
      const qty = p.quantity || 0;
      const matchesS =
        stockFilter === 'all' ||
        (stockFilter === 'low' && qty <= 5) ||
        (stockFilter === 'out' && qty === 0) ||
        (stockFilter === 'healthy' && qty > 5);
      return matchesQ && matchesC && matchesS;
    });
  }, [products, query, catFilter, stockFilter]);

  const openCreate = () => {
    setForm(emptyForm);
    setModal({ mode: 'create' });
  };
  const openEdit = (p) => {
    setForm({
      name: p.name || '',
      description: p.description || '',
      price: p.price ?? '',
      categoryId: p.categoryId || '',
    });
    setModal({ mode: 'edit', id: p.id });
  };

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setNotice(null);
    try {
      if (modal.mode === 'create') {
        // Backend create DTO: { name, description, price, categoryId }
        await productsApi.create({
          name: form.name,
          description: form.description,
          price: Number(form.price),
          categoryId: form.categoryId,
        });
        setNotice({ type: 'ok', text: 'Product created.' });
      } else {
        // Backend update DTO (ProductUpdateRequest) accepts ONLY
        // name/description/price/categoryId — quantity & active are read-only here.
        const payload = {
          ...(form.name ? { name: form.name } : {}),
          ...(form.description ? { description: form.description } : {}),
          ...(form.price !== '' ? { price: Number(form.price) } : {}),
          ...(form.categoryId ? { categoryId: form.categoryId } : {}),
        };
        await productsApi.update(modal.id, payload);
        setNotice({ type: 'ok', text: 'Product updated.' });
      }
      setModal(null);
      await load();
    } catch (err) {
      setNotice({ type: 'error', text: friendlyError(err, 'Could not save the product. Please try again.') });
    } finally {
      setSaving(false);
    }
  };

  const confirmRemove = async () => {
    if (!pendingDelete) return;
    setDeleting(true);
    setNotice(null);
    try {
      await productsApi.remove(pendingDelete.id);
      setNotice({ type: 'ok', text: `“${pendingDelete.name}” deleted.` });
      setPendingDelete(null);
      await load();
    } catch (err) {
      setPendingDelete(null);
      if (isForeignKeyError(err)) {
        setNotice({
          type: 'error',
          text: `Couldn't delete “${pendingDelete.name}” — it has order or stock history linked to it. To delete it, that history must be archived first (currently a database-level task; ask your admin). The product is unchanged.`,
        });
      } else {
        setNotice({ type: 'error', text: friendlyError(err, `Couldn't delete “${pendingDelete.name}”. Please try again.`) });
      }
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Products</h1>
          <p>{products.length} SKUs · live from <span className="kbd">GET /api/v1/products</span>. Create / edit / delete require ADMIN.</p>
        </div>
        {isAdmin && <button className="btn" onClick={openCreate}>+ New product</button>}
      </div>

      {notice && <div className={`alert ${notice.type}`}>{notice.text}</div>}
      {error && <div className="alert error">{error}</div>}

      <div className="toolbar">
        <input className="input search" placeholder="Search name or description…" value={query} onChange={(e) => setQuery(e.target.value)} />
        <select className="select" value={catFilter} onChange={(e) => setCatFilter(e.target.value)}>
          <option value="all">All categories</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select className="select" value={stockFilter} onChange={(e) => setStockFilter(e.target.value)}>
          <option value="all">Any stock</option>
          <option value="healthy">Healthy (&gt; 5)</option>
          <option value="low">Low (≤ 5)</option>
          <option value="out">Out (0)</option>
        </select>
      </div>

      {loading ? (
        <div className="card">Loading products…</div>
      ) : filtered.length === 0 ? (
        <div className="card empty"><b>No products match</b>Try clearing filters, or add a product as admin.</div>
      ) : (
        <div className="table-wrap">
          <table>
            <thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th>{isAdmin && <th style={{ textAlign: 'right' }}>Actions</th>}</tr></thead>
            <tbody>
              {filtered.map((p) => {
                const qty = p.quantity || 0;
                return (
                  <tr key={p.id}>
                    <td><b>{p.name}</b><br /><span style={{ color: '#6b7280', fontSize: 12 }}>{p.description?.slice(0, 60) || '—'}</span></td>
                    <td><span className="badge">{catName(p.categoryId)}</span></td>
                    <td className="mono">₹{p.price}</td>
                    <td><span className={`badge ${qty === 0 ? 'red' : qty <= 5 ? 'amber' : 'green'}`}>{qty} units</span></td>
                    <td><span className={`badge ${p.active === false ? 'red' : 'dark'}`}>{p.active === false ? 'inactive' : 'active'}</span></td>
                    {isAdmin && (
                      <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                        <button className="btn ghost small" onClick={() => openEdit(p)}>Edit</button>{' '}
                        <button className="btn danger small" onClick={() => setPendingDelete(p)}>Delete</button>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {modal && (
        <div className="modal-backdrop" onClick={() => setModal(null)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h3>{modal.mode === 'create' ? 'New product' : 'Edit product'}</h3>
            <p className="sub">{modal.mode === 'create' ? 'POST /api/v1/products (ADMIN)' : 'PATCH /api/v1/products/{id} (ADMIN)'}</p>
            <form onSubmit={save}>
              <div className="field"><label className="label">Name</label>
                <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
              </div>
              <div className="field"><label className="label">Description</label>
                <textarea className="textarea" rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
              </div>
              <div className="field"><label className="label">Price (₹)</label>
                <input className="input" type="number" step="0.01" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} required={modal.mode === 'create'} />
              </div>
              {modal.mode === 'create' && (
                <p style={{ fontSize: 12, color: '#6b7280', margin: '0 0 14px' }}>
                  New products start at 0 stock — restock them from the <b>Inventory</b> tab (admin).
                </p>
              )}
              {modal.mode === 'edit' && (
                <p style={{ fontSize: 12, color: '#6b7280', margin: '0 0 14px' }}>
                  Stock & status are read-only — <span className="kbd">PATCH /products</span> accepts
                  name/description/price/category only. Stock changes when orders are placed.
                </p>
              )}
              <div className="field"><label className="label">Category</label>
                <select className="select" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} required={modal.mode === 'create'}>
                  <option value="">Select…</option>
                  {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
                {categories.length === 0 && <p style={{ fontSize: 12, color: '#6b7280' }}>No categories cached — create one under Categories first, then paste its ID.</p>}
              </div>
              <div className="row-end">
                <button type="button" className="btn ghost" onClick={() => setModal(null)}>Cancel</button>
                <button className="btn" disabled={saving}>{saving ? 'Saving…' : 'Save product'}</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {pendingDelete && (
        <ConfirmDelete
          title="Delete product?"
          itemName={pendingDelete.name}
          requirements={[
            'No orders contain this product.',
            'No stock transactions reference it (purchases, sales, adjustments).',
          ]}
          blockedHint="If history exists, deletion is blocked to protect your records. Archive that history first (currently needs direct database access) — the product will stay exactly as it is."
          confirmLabel="Delete product"
          busy={deleting}
          onCancel={() => setPendingDelete(null)}
          onConfirm={confirmRemove}
        />
      )}
    </>
  );
}
