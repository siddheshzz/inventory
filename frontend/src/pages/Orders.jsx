import { useEffect, useMemo, useState } from 'react';
import ConfirmDelete from '../components/ConfirmDelete';
import { friendlyError, ordersApi, productsApi } from '../lib/api';
import { useAuth } from '../lib/auth';
import { customerName, newestFirst, titleCase } from '../lib/presentation';

const money = (n) => `₹${Number(n || 0).toLocaleString('en-IN')}`;
const dateTime = (v) => v ? new Date(v).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : 'Date unavailable';

const STATUSES = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
const TERMINAL = ['DELIVERED', 'CANCELLED'];
const NEXT = { PENDING: 'CONFIRMED', CONFIRMED: 'PROCESSING', PROCESSING: 'SHIPPED', SHIPPED: 'DELIVERED' };
const ACTION = { PENDING: 'Confirm order', CONFIRMED: 'Start processing', PROCESSING: 'Mark as shipped', SHIPPED: 'Mark as delivered' };

function statusBadge(status) {
  if (status === 'CANCELLED') return 'red';
  if (status === 'DELIVERED') return 'green';
  if (status === 'PENDING') return 'amber';
  return 'dark';
}

function OrderCard({
  order, expanded, onToggle, isAdmin,
  products, onEnsureProducts, onMutated, onNotify,
}) {
  const items = order.items || [];
  const editable = order.status === 'PENDING' && order.paymentStatus === 'PENDING' && !order.checkoutPending;
  const terminal = TERMINAL.includes(order.status);

  const [addOpen, setAddOpen] = useState(false);
  const [addPid, setAddPid] = useState('');
  const [addQty, setAddQty] = useState(1);
  const [busy, setBusy] = useState(false);
  const [statusSel, setStatusSel] = useState(order.status);
  const [confirmingRemove, setConfirmingRemove] = useState(null); // itemId
  const [confirmCancel, setConfirmCancel] = useState(false);
  useEffect(() => { setStatusSel(order.status); }, [order.status]);

  const addProduct = products.find((p) => p.id === addPid) || null;
  const addMax = addProduct?.quantity || 0;

  const mutate = async (fn, okText) => {
    setBusy(true);
    try {
      const updated = await fn();
      onMutated(updated);
      onNotify({ type: 'ok', text: okText(updated) });
      return updated;
    } catch (err) {
      onNotify({ type: 'error', text: friendlyError(err, 'Could not update the order. Please try again.') });
      return null;
    } finally {
      setBusy(false);
    }
  };

  const doAddItem = () => {
    if (!addPid || addQty < 1) return;
    mutate(
      () => ordersApi.addItem(order.id, { productId: addPid, quantity: Number(addQty) }),
      (u) => `Item added — order is now ${money(u.grandTotal)} (${u.items.length} lines). Stock decremented.`,
    ).then((u) => {
      if (u) { setAddOpen(false); setAddPid(''); setAddQty(1); }
    });
  };

  const doRemoveItem = (item) => {
    mutate(
      () => ordersApi.removeItem(order.id, item.id),
      () => `“${item.productName}” removed — stock restored.`,
    ).then((u) => { if (u) setConfirmingRemove(null); });
  };

  const doStatus = (status) => {
    mutate(
      () => ordersApi.updateStatus(order.id, status),
      (u) => status === 'CANCELLED'
        ? `Order cancelled — stock for all lines restored.`
        : `Order status → ${u.status}.`,
    ).then((u) => { if (u) setConfirmCancel(false); });
  };

  return (
    <div className="admin-order">
      <button type="button" className="admin-order-row" aria-expanded={expanded}
        onClick={onToggle}
      >
        <span><small>Customer</small><b>{customerName(order)}</b><span className="cell-sub">{order.userPhone}</span></span>
        <span><small>Purchased on</small><b>{dateTime(order.createdAt)}</b><span className="cell-sub">#{String(order.id).slice(0, 8)}</span></span>
        <span><small>Products</small><b>{items.map(i => i.productName).join(', ')}</b><span className="cell-sub">{items.reduce((s, i) => s + (i.quantity || 0), 0)} units</span></span>
        <span><small>Status / payment</small><span className={`badge ${statusBadge(order.status)}`}>{titleCase(order.status)}</span><span className="cell-sub">{order.paymentStatus === 'CONFIRMED' ? 'Paid' : 'Payment pending'}</span></span>
        <span><small>Total</small><b>{money(order.grandTotal)}</b><span className="cell-sub">{expanded ? 'Close details ↑' : 'Manage order ↓'}</span></span>
      </button>

      {expanded && (
        <div style={{ marginTop: 10, borderTop: '1px solid #f1f1f1', paddingTop: 10, fontSize: 13 }}>
          {items.map((i) => (
            <div key={i.id} style={{ display: 'flex', gap: 8, padding: '5px 0', alignItems: 'center' }}>
              <span style={{ flex: 1 }}><b>{i.productName}</b> × {i.quantity}</span>
              <span className="mono" style={{ color: '#6b7280' }}>{money(i.unitPrice)} each</span>
              <b className="mono">{money(i.lineTotal)}</b>
              {editable && (
                items.length === 1 ? (
                  <span className="badge" title="The last line can't be removed — cancel the order instead">locked</span>
                ) : confirmingRemove === i.id ? (
                  <>
                    <button className="btn danger small" disabled={busy} onClick={() => doRemoveItem(i)}>Confirm</button>
                    <button className="btn ghost small" onClick={() => setConfirmingRemove(null)}>Keep</button>
                  </>
                ) : (
                  <button className="btn ghost small" onClick={() => setConfirmingRemove(i.id)}>Remove</button>
                )
              )}
            </div>
          ))}

          <div style={{ display: 'flex', gap: 8, padding: '6px 0 0', color: '#6b7280' }}>
            <span style={{ flex: 1 }}>Subtotal {money(order.subtotal)} · Discount {money(order.discount)} · Tax {money(order.tax)} · Shipping {money(order.shippingCharges)}</span>
          </div>
          <p className="muted">Last updated: {dateTime(order.updatedAt)}</p>
          <details className="technical"><summary>Full order & customer references</summary><p>Order: <code>{order.id}</code><br />Customer: <code>{order.userId}</code></p></details>

          {editable && (
            <div style={{ marginTop: 10, borderTop: '1px dashed #eee', paddingTop: 10 }}>
              {!addOpen ? (
                <button
                  className="btn ghost small"
                  onClick={() => { onEnsureProducts(); setAddOpen(true); }}
                >
                  + Add item (PENDING order)
                </button>
              ) : (
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'end' }}>
                  <div style={{ flex: '1 1 220px' }}>
                    <label className="label">Product (in stock only)</label>
                    <select className="select" value={addPid} onChange={(e) => { setAddPid(e.target.value); setAddQty(1); }}>
                      <option value="">Select…</option>
                      {products.filter((p) => p.active !== false && (p.quantity || 0) > 0).map((p) => (
                        <option key={p.id} value={p.id}>{p.name} — {money(p.price)} · stock {p.quantity}</option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <label className="label">Qty (max {addMax})</label>
                    <input
                      className="input" type="number" min="1" max={Math.max(1, addMax)}
                      value={addQty} onChange={(e) => setAddQty(Math.max(1, Math.min(addMax, Number(e.target.value) || 1)))}
                      style={{ width: 90 }}
                    />
                  </div>
                  <button className="btn small" disabled={busy || !addPid} onClick={doAddItem}>
                    {busy ? 'Adding…' : 'Add'}
                  </button>
                  <button className="btn ghost small" onClick={() => setAddOpen(false)}>Cancel</button>
                </div>
              )}
            </div>
          )}

          {terminal && (
            <div className="alert info" style={{ marginTop: 10, marginBottom: 0 }}>
              {order.status} orders are terminal — no further edits or status changes allowed.
            </div>
          )}

          {isAdmin && !terminal && (
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'end', marginTop: 10, borderTop: '1px dashed #eee', paddingTop: 10 }}>
              {order.checkoutPending ? <p className="alert info">Checkout is in progress. The customer must complete payment before this order can advance.</p> : <button className="btn" disabled={busy} onClick={() => doStatus(NEXT[order.status])}>{ACTION[order.status]}</button>}
              <div>
                <label className="label">Change status</label>
                <select className="select" disabled={order.checkoutPending} value={statusSel} onChange={(e) => setStatusSel(e.target.value)}>
                  {[order.status, NEXT[order.status], ...(order.status !== 'SHIPPED' && order.paymentStatus !== 'CONFIRMED' ? ['CANCELLED'] : [])].filter(Boolean).map((s) => <option key={s} value={s}>{titleCase(s)}</option>)}
                </select>
              </div>
              <button
                className="btn small"
                disabled={busy || order.checkoutPending || statusSel === order.status}
                onClick={() => (statusSel === 'CANCELLED' ? setConfirmCancel(true) : doStatus(statusSel))}
              >
                Apply status
              </button>
            </div>
          )}

          {confirmCancel && (
            <ConfirmDelete
              title="Cancel order?"
              itemName={`Order ${String(order.id).slice(0, 8)}… (${money(order.grandTotal)})`}
              sub={<>Cancelling is <b>terminal</b> — the order can never be edited or reopened.</>}
              requirements={[
                'Stock for every line is restored (RETURN transactions).',
                'This cannot be undone.',
              ]}
              blockedHint="Terminal orders (DELIVERED / CANCELLED) reject further changes."
              confirmLabel="Cancel order"
              ackLabel="I understand cancelling is terminal and restores stock."
              busy={busy}
              onCancel={() => setConfirmCancel(false)}
              onConfirm={() => doStatus('CANCELLED')}
            />
          )}
        </div>
      )}
    </div>
  );
}

export default function Orders() {
  const { profile, isAdmin } = useAuth();
  const [orders, setOrders] = useState(null); // admin list (GET), null = not loaded / not allowed
  const [loadError, setLoadError] = useState(null);
  const [sessionOrders, setSessionOrders] = useState([]); // orders placed from this UI
  const [expanded, setExpanded] = useState({});
  const [modal, setModal] = useState(false);
  const [products, setProducts] = useState([]);
  const [productsLoaded, setProductsLoaded] = useState(false);
  const [query, setQuery] = useState('');
  const [lines, setLines] = useState({}); // productId -> qty
  const [discount, setDiscount] = useState('');
  // Fresh key per submission: retries of the same form return the original
  // order instead of a duplicate; same key + different body -> 409.
  const [idemKey, setIdemKey] = useState(() => crypto.randomUUID());
  const [placing, setPlacing] = useState(false);
  const [notice, setNotice] = useState(null);
  const [confirmation, setConfirmation] = useState(null);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('ALL');

  const loadOrders = async () => {
    if (!isAdmin) return;
    setLoadError(null);
    try {
      const list = await ordersApi.list();
      setOrders(Array.isArray(list) ? list : []);
    } catch (e) { setLoadError(friendlyError(e, 'Could not load orders.')); }
  };

  useEffect(() => { loadOrders(); }, [isAdmin]);

  const ensureProducts = async () => {
    if (productsLoaded) return;
    try {
      const list = await productsApi.list();
      setProducts(Array.isArray(list) ? list : []);
      setProductsLoaded(true);
    } catch (e) {
      setNotice({ type: 'error', text: friendlyError(e, 'Could not load products.') });
    }
  };

  const refreshProducts = async () => {
    try { setProducts(await productsApi.list()); setProductsLoaded(true); } catch { setProductsLoaded(false); }
  };

  // Merge a mutation response (updated order) into every visible list.
  const replaceOrder = (updated) => {
    const swap = (arr) => (arr || []).map((o) => (o.id === updated.id ? updated : o));
    setOrders((prev) => (prev === null ? prev : swap(prev)));
    setSessionOrders((prev) => swap(prev));
  };

  const notify = (n) => setNotice(n);

  const openModal = async () => {
    setNotice(null);
    setConfirmation(null);
    try {
      const list = await productsApi.list();
      setProducts(Array.isArray(list) ? list : []);
      setProductsLoaded(true);
    } catch (e) {
      setNotice({ type: 'error', text: friendlyError(e, 'Could not load products for the order form.') });
    }
    setLines({});
    setDiscount('');
    setQuery('');
    setIdemKey(crypto.randomUUID());
    setModal(true);
  };

  // Inactive (soft-deleted) products are hidden from pickers — the backend
  // has no reactivate path, so ordering them would strand stock.
  const orderableProducts = useMemo(
    () => products.filter((p) => p.active !== false),
    [products],
  );

  const visibleProducts = useMemo(() => {
    const q = query.toLowerCase();
    return orderableProducts.filter(
      (p) => !q || p.name?.toLowerCase().includes(q) || p.description?.toLowerCase().includes(q),
    );
  }, [orderableProducts, query]);

  const totals = useMemo(() => {
    let subtotal = 0;
    let count = 0;
    for (const p of products) {
      const qty = lines[p.id] || 0;
      if (qty > 0) {
        subtotal += Number(p.price || 0) * qty;
        count += qty;
      }
    }
    const disc = Math.max(0, Number(discount) || 0);
    return { subtotal, disc, grand: Math.max(0, subtotal - disc), count };
  }, [products, lines, discount]);

  const setQty = (p, qty) => {
    const clamped = Math.max(0, Math.min(p.quantity || 0, qty));
    setLines((prev) => {
      const next = { ...prev };
      if (clamped === 0) delete next[p.id];
      else next[p.id] = clamped;
      return next;
    });
  };

  const placeOrder = async (e) => {
    e.preventDefault();
    setNotice(null);
    const items = Object.entries(lines).map(([productId, quantity]) => ({ productId, quantity }));
    if (items.length === 0) {
      setNotice({ type: 'error', text: 'Add at least one item to the order.' });
      return;
    }
    setPlacing(true);
    try {
      const payload = { items, ...(totals.disc > 0 ? { discount: totals.disc } : {}) };
      const created = await ordersApi.create(payload, idemKey);
      setConfirmation(created);
      setSessionOrders((prev) => [created, ...prev].slice(0, 50));
      setModal(false);
      setNotice({ type: 'ok', text: `Order placed — ${created.id.slice(0, 8)}… (${money(created.grandTotal)}). Stock decremented.` });
      if (isAdmin) await loadOrders();
      await refreshProducts();
    } catch (err) {
      setNotice({ type: 'error', text: friendlyError(err, 'Could not place the order. Please try again.') });
    } finally {
      setPlacing(false);
    }
  };

  // USER sees session orders; ADMIN sees server list (plus just-placed if list reload lags — dedupe by id).
  const shownOrders = useMemo(() => {
    if (isAdmin) {
      const seen = new Set();
      return [...sessionOrders, ...(orders || [])].filter((o) =>
        o?.id && !seen.has(o.id) ? (seen.add(o.id), true) : false,
      );
    }
    return sessionOrders;
  }, [isAdmin, orders, sessionOrders]);
  const filtered = [...shownOrders].sort(newestFirst).filter(o => (filter === 'ALL' || o.status === filter) && `${customerName(o)} ${o.userPhone} ${o.id} ${(o.items || []).map(i => i.productName).join(' ')}`.toLowerCase().includes(search.toLowerCase()));

  return (
    <>
      <div className="page-head">
        <div>
          <span className="section-kicker">FULFILMENT DESK</span><h1>Customer orders</h1>
          <p>Customer details, purchases and next actions. Sorted newest first.</p>
        </div>
        <button className="btn" onClick={openModal}>+ Order for myself</button>
      </div>

      {notice && <div className={`alert ${notice.type}`}>{notice.text}</div>}
      {loadError && <div className="alert error">Could not load orders: {loadError}</div>}

      {confirmation && (
        <div className="card" style={{ marginBottom: 14, borderColor: '#a7f3d0' }}>
          <h3 style={{ margin: '0 0 4px' }}>Order confirmed · <span className="mono">{confirmation.id.slice(0, 8)}…</span></h3>
          <p style={{ margin: '0 0 10px', color: '#6b7280', fontSize: 13 }}>
            <span className={`badge ${statusBadge(confirmation.status)}`}>{confirmation.status}</span>{' '}
            <span className="badge">{confirmation.paymentStatus}</span>{' '}
            Subtotal {money(confirmation.subtotal)} − Discount {money(confirmation.discount)} = <b>{money(confirmation.grandTotal)}</b>
          </p>
          <div style={{ fontSize: 13 }}>
            {(confirmation.items || []).map((i) => (
              <div key={i.id} style={{ display: 'flex', gap: 8, padding: '3px 0' }}>
                <span style={{ flex: 1 }}>{i.productName} × {i.quantity}</span>
                <b className="mono">{money(i.lineTotal)}</b>
              </div>
            ))}
          </div>
        </div>
      )}

      {!isAdmin && (
        <div className="alert info">
          Order history (<span className="kbd">GET /orders</span>) requires ADMIN. Below are orders you placed in this session — you can still edit their PENDING lines.
        </div>
      )}

      <div className="toolbar"><input className="input search" aria-label="Search orders" placeholder="Search customer, phone, product or reference…" value={search} onChange={e => setSearch(e.target.value)} /><select className="select" aria-label="Order status" value={filter} onChange={e => setFilter(e.target.value)}><option value="ALL">All statuses</option>{STATUSES.map(s => <option key={s} value={s}>{titleCase(s)}</option>)}</select><button className="btn ghost" onClick={loadOrders}>Refresh</button><span className="muted">{filtered.length} orders · Newest first</span></div>
      {isAdmin && orders === null && !loadError ? (
        <div className="card">Loading orders…</div>
      ) : filtered.length === 0 ? (
        <div className="card empty"><b>No matching orders</b>Orders will appear here after customers check out. Try clearing your filters.</div>
      ) : (
        <div style={{ display: 'grid', gap: 10 }}>
          {filtered.map((o) => (
            <OrderCard
              key={o.id}
              order={o}
              expanded={!!expanded[o.id]}
              onToggle={() => setExpanded((prev) => ({ ...prev, [o.id]: !prev[o.id] }))}
              isAdmin={isAdmin}
              products={products}
              onEnsureProducts={ensureProducts}
              onMutated={(u) => { replaceOrder(u); refreshProducts(); }}
              onNotify={notify}
            />
          ))}
        </div>
      )}

      {modal && (
        <div className="modal-backdrop" onClick={() => setModal(false)}>
          <div className="modal" style={{ width: 'min(640px, 100%)' }} onClick={(e) => e.stopPropagation()}>
            <h3>New order</h3>
            <p className="sub">
              Ordering as <b>{profile?.name || profile?.phoneNumber}</b> ·{' '}
              <span className="kbd">POST /orders (you)</span> · key{' '}
              <span className="mono" style={{ fontSize: 11 }}>{idemKey.slice(0, 8)}…</span> (safe to retry)
            </p>
            <form onSubmit={placeOrder}>
              <input
                className="input"
                placeholder="Search products…"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                style={{ marginBottom: 10 }}
              />
              <div style={{ maxHeight: 300, overflowY: 'auto', display: 'grid', gap: 8 }}>
                {visibleProducts.map((p) => {
                  const qty = lines[p.id] || 0;
                  const out = (p.quantity || 0) <= 0;
                  return (
                    <div
                      key={p.id}
                      style={{
                        border: '1px solid #eee', borderRadius: 10, padding: '8px 10px',
                        display: 'flex', alignItems: 'center', gap: 10,
                        opacity: out ? 0.55 : 1,
                      }}
                    >
                      <div style={{ flex: 1 }}>
                        <b style={{ fontSize: 14 }}>{p.name}</b><br />
                        <span className="mono" style={{ fontSize: 12, color: '#6b7280' }}>
                          {money(p.price)} · stock {p.quantity ?? 0}
                        </span>
                      </div>
                      {out ? (
                        <span className="badge red">out of stock</span>
                      ) : (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                          <button type="button" className="btn ghost small" onClick={() => setQty(p, qty - 1)}>−</button>
                          <b style={{ minWidth: 20, textAlign: 'center' }}>{qty}</b>
                          <button type="button" className="btn ghost small" onClick={() => setQty(p, qty + 1)}>+</button>
                        </div>
                      )}
                    </div>
                  );
                })}
                {visibleProducts.length === 0 && (
                  <div className="empty"><b>No products</b>Add products first (admin).</div>
                )}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 10, marginTop: 12 }}>
                <div className="field" style={{ margin: 0 }}>
                  <label className="label">Discount (₹, optional)</label>
                  <input
                    className="input" type="number" min="0" step="0.01"
                    value={discount} onChange={(e) => setDiscount(e.target.value)}
                    placeholder="0"
                  />
                </div>
                <div style={{ fontSize: 13, alignSelf: 'end', textAlign: 'right' }}>
                  Subtotal {money(totals.subtotal)} − {money(totals.disc)} = <b>{money(totals.grand)}</b>
                  <br /><span style={{ color: '#6b7280' }}>{totals.count} units</span>
                </div>
              </div>

              <div className="row-end">
                <button type="button" className="btn ghost" onClick={() => setModal(false)}>Cancel</button>
                <button className="btn" disabled={placing || totals.count === 0}>
                  {placing ? 'Placing…' : `Place order · ${money(totals.grand)}`}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </>
  );
}
