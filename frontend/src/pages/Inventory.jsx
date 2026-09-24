import { useEffect, useMemo, useState } from 'react';
import { friendlyError, inventoryApi, productsApi } from '../lib/api';
import { useAuth } from '../lib/auth';

export default function Inventory() {
  const { profile, isAdmin } = useAuth();
  const [products, setProducts] = useState([]);
  const [query, setQuery] = useState('');
  const [selectedId, setSelectedId] = useState(null);
  const [verify, setVerify] = useState(null);
  const [verifying, setVerifying] = useState(false);
  const [restockQty, setRestockQty] = useState('');
  const [reference, setReference] = useState('');
  const [purchasing, setPurchasing] = useState(false);
  const [notice, setNotice] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const list = await productsApi.list();
        const arr = Array.isArray(list) ? list : [];
        setProducts(arr);
        if (arr.length > 0) setSelectedId(arr[0].id);
      } catch (e) {
        setNotice({ type: 'error', text: friendlyError(e, 'Could not load products.') });
      }
    })();
  }, []);

  const selected = useMemo(
    () => products.find((p) => p.id === selectedId) || null,
    [products, selectedId],
  );

  const visible = useMemo(() => {
    const q = query.toLowerCase();
    return products.filter(
      (p) => !q || p.name?.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q),
    );
  }, [products, query]);

  const refreshProducts = async () => {
    try {
      const list = await productsApi.list();
      setProducts(Array.isArray(list) ? list : []);
    } catch { /* keep stale list on error */ }
  };

  const runVerify = async (id) => {
    const pid = id || selectedId;
    if (!pid) return;
    setVerifying(true);
    setNotice(null);
    try {
      const res = await inventoryApi.verifyStock(pid);
      setVerify(res);
    } catch (e) {
      setVerify(null);
      setNotice({ type: 'error', text: friendlyError(e, 'Could not verify stock. Please try again.') });
    } finally {
      setVerifying(false);
    }
  };

  useEffect(() => {
    if (selectedId && isAdmin) runVerify(selectedId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedId]);

  const runPurchase = async (e) => {
    e.preventDefault();
    setNotice(null);
    if (!profile?.id) {
      setNotice({ type: 'error', text: 'Profile not loaded yet — reload the page.' });
      return;
    }
    const qty = Number(restockQty);
    if (!selectedId || !Number.isInteger(qty) || qty < 1) {
      setNotice({ type: 'error', text: 'Enter a whole quantity of at least 1.' });
      return;
    }
    setPurchasing(true);
    try {
      const res = await inventoryApi.purchase(profile.id, {
        productId: selectedId,
        quantity: qty,
        ...(reference.trim() ? { reference: reference.trim() } : {}),
      });
      setVerify(res);
      setRestockQty('');
      setReference('');
      setNotice({ type: 'ok', text: `Restocked +${qty} — cached ${res.cachedQuantity}, ledger ${res.ledgerQuantity}.` });
      await refreshProducts();
    } catch (err) {
      setNotice({ type: 'error', text: friendlyError(err, 'Could not restock. Please try again.') });
    } finally {
      setPurchasing(false);
    }
  };

  if (!isAdmin) {
    return (
      <>
        <div className="page-head"><div><h1>Inventory</h1><p>Admin-only section.</p></div></div>
        <div className="card empty">
          <b>Restricted — ADMIN role required</b>
          Both <span className="kbd">/api/v1/inventory/…</span> endpoints are guarded by
          <span className="kbd"> @PreAuthorize("hasRole('ADMIN')")</span>.
          Sign in with the admin number to verify stock and restock.
        </div>
      </>
    );
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Inventory</h1>
          <p>
            Verify cached stock against the transaction ledger, and restock via
            <span className="kbd"> POST /purchase</span> (writes a <span className="kbd">PURCHASE</span> transaction).
          </p>
        </div>
      </div>

      {notice && <div className={`alert ${notice.type}`}>{notice.text}</div>}

      <div className="grid-2">
        <div className="card">
          <h3 style={{ margin: '0 0 4px' }}>Products</h3>
          <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}>Select one to verify / restock</p>
          <input
            className="input" placeholder="Search products…"
            value={query} onChange={(e) => setQuery(e.target.value)}
            style={{ marginBottom: 10 }}
          />
          <div style={{ maxHeight: 380, overflowY: 'auto', display: 'grid', gap: 8 }}>
            {visible.map((p) => (
              <div
                key={p.id}
                onClick={() => setSelectedId(p.id)}
                style={{
                  border: p.id === selectedId ? '2px solid #111' : '1px solid #eee',
                  borderRadius: 10, padding: '8px 10px', cursor: 'pointer',
                  display: 'flex', alignItems: 'center', gap: 10,
                }}
              >
                <div style={{ flex: 1 }}>
                  <b style={{ fontSize: 14 }}>{p.name}</b><br />
                  <span className="mono" style={{ fontSize: 12, color: '#6b7280' }}>₹{p.price}</span>
                </div>
                <span className={`badge ${(p.quantity || 0) === 0 ? 'red' : (p.quantity || 0) <= 5 ? 'amber' : 'green'}`}>
                  {p.quantity ?? 0} units
                </span>
              </div>
            ))}
            {visible.length === 0 && <div className="empty"><b>No products</b></div>}
          </div>
        </div>

        <div>
          <div className="card" style={{ marginBottom: 14 }}>
            <h3 style={{ margin: '0 0 4px' }}>Stock verify</h3>
            <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}>
              <span className="kbd">GET /inventory/products/{selectedId ? `${selectedId.slice(0, 8)}…` : '…'}/stock-verify</span>
            </p>
            {verifying ? (
              'Checking…'
            ) : verify ? (
              <div style={{ display: 'grid', gap: 10 }}>
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', alignItems: 'center' }}>
                  <span className={`badge ${verify.match ? 'green' : 'red'}`} style={{ fontSize: 14 }}>
                    {verify.match ? '✓ MATCH' : '✗ MISMATCH'}
                  </span>
                  <span style={{ fontSize: 13, color: '#6b7280' }}>{selected?.name}</span>
                </div>
                <div className="grid-2" style={{ gridTemplateColumns: '1fr 1fr' }}>
                  <div style={{ border: '1px solid #eee', borderRadius: 10, padding: '10px 12px' }}>
                    <small style={{ color: '#6b7280', fontWeight: 600 }}>CACHED (product.quantity)</small>
                    <strong style={{ display: 'block', fontSize: 26 }}>{verify.cachedQuantity}</strong>
                  </div>
                  <div style={{ border: '1px solid #eee', borderRadius: 10, padding: '10px 12px' }}>
                    <small style={{ color: '#6b7280', fontWeight: 600 }}>LEDGER (Σ transactions)</small>
                    <strong style={{ display: 'block', fontSize: 26 }}>{verify.ledgerQuantity}</strong>
                  </div>
                </div>
                {!verify.match && (
                  <div className="alert error" style={{ margin: 0 }}>
                    Cached and ledger disagree — stock was changed outside a transaction
                    (e.g. manual DB edit). Restock below moves both together; for a pure
                    correction the backend needs an adjust endpoint (not built yet).
                  </div>
                )}
                <button className="btn ghost" onClick={() => runVerify()}>Re-check</button>
              </div>
            ) : (
              <div className="empty"><b>Nothing checked yet</b>Select a product.</div>
            )}
          </div>

          <div className="card">
            <h3 style={{ margin: '0 0 4px' }}>Restock (purchase)</h3>
            <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}>
              <span className="kbd">POST /inventory/purchase?adminUserId=…</span> · adds stock + writes PURCHASE tx
            </p>
            <form onSubmit={runPurchase}>
              <div className="field"><label className="label">Quantity to add</label>
                <input
                  className="input" type="number" min="1" step="1"
                  value={restockQty} onChange={(e) => setRestockQty(e.target.value)}
                  placeholder="e.g. 20" required
                />
              </div>
              <div className="field"><label className="label">Reference (optional)</label>
                <input
                  className="input" value={reference}
                  onChange={(e) => setReference(e.target.value)}
                  placeholder="e.g. Supplier Invoice #123"
                />
              </div>
              <button className="btn" disabled={purchasing || !selectedId} style={{ width: '100%' }}>
                {purchasing ? 'Restocking…' : `Restock ${selected?.name?.slice(0, 24) || 'product'}`}
              </button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
}
