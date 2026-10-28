import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { friendlyError, ordersApi, productsApi } from '../lib/api';
import { useAuth } from '../lib/auth';
import { money } from '../lib/presentation';
import { prepareCheckout, payOrder } from '../lib/checkout';
import RallyCar from '../components/RallyCar';
import Modal from '../components/Modal';

export default function Catalogue() {
  const { profile } = useAuth();
  return profile ? <Store key={profile.id} profile={profile} /> : <div className="card">Loading your shop…</div>;
}

function Store({ profile }) {
  const navigate = useNavigate();
  const cartKey = `stockline.cart.${profile.id}`;
  const [products, setProducts] = useState([]), [loading, setLoading] = useState(true);
  const [cart, setCart] = useState(() => { try { return JSON.parse(sessionStorage.getItem(cartKey)) || {}; } catch { return {}; } });
  const [query, setQuery] = useState(''), [category, setCategory] = useState('all'), [sort, setSort] = useState('name');
  const [notice, setNotice] = useState(null), [busy, setBusy] = useState(false), [review, setReview] = useState(false);
  const [detail, setDetail] = useState(null);
  const submitting = useRef(false);
  useEffect(() => { sessionStorage.setItem(cartKey, JSON.stringify(cart)); }, [cartKey, cart]);
  const load = async () => { try { setProducts(await productsApi.list()); } catch (e) { setNotice(friendlyError(e, 'Could not load products.')); } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);
  const categories = useMemo(() => [...new Set(products.filter(p => p.active !== false).map(p => p.categoryName).filter(Boolean))].sort(), [products]);
  const visible = useMemo(() => products.filter(p => p.active !== false && (category === 'all' || p.categoryName === category) && `${p.name} ${p.description || ''}`.toLowerCase().includes(query.toLowerCase())).sort((a, b) => sort === 'low' ? a.price - b.price : sort === 'high' ? b.price - a.price : a.name.localeCompare(b.name)), [products, category, query, sort]);
  const selected = products.filter(p => cart[p.id] > 0);
  const count = selected.reduce((sum, p) => sum + cart[p.id], 0);
  const total = selected.reduce((sum, p) => sum + Number(p.price) * cart[p.id], 0);
  const invalid = selected.some(p => p.active === false || cart[p.id] > p.quantity || !Number.isInteger(cart[p.id]));
  const quantity = (p, n) => { if (busy) return; setCart(c => ({ ...c, [p.id]: Math.max(0, Math.min(p.quantity, Math.floor(Number(n) || 0))) })); };
  const checkout = async () => {
    if (submitting.current || invalid || !selected.length) return;
    submitting.current = true; setBusy(true); setNotice(null);
    let order;
    try {
      const config = await prepareCheckout();
      const payload = { items: selected.map(p => ({ productId: p.id, quantity: cart[p.id] })) };
      // Retain the key across uncertain network responses and same-cart retries.
      const attemptKey = `stockline.checkout.${profile.id}`;
      let attempt;
      try { attempt = JSON.parse(sessionStorage.getItem(attemptKey)); } catch { /* start a new attempt */ }
      if (!attempt || attempt.payload !== JSON.stringify(payload)) { attempt = { key: crypto.randomUUID(), payload: JSON.stringify(payload) }; sessionStorage.setItem(attemptKey, JSON.stringify(attempt)); }
      order = await ordersApi.create(payload, attempt.key);
      setCart({}); sessionStorage.removeItem(attemptKey); setReview(false);
      const result = await payOrder(order, profile, config);
      navigate('/shop/orders', { state: { orderId: order.id, message: result.paid ? 'Payment verified. Thanks for your order!' : 'Your order is saved. You can resume payment here.' } });
    } catch (e) {
      const text = friendlyError(e, 'Checkout could not be completed. Please try again.');
      if (order) navigate('/shop/orders', { state: { orderId: order.id, message: text } });
      else setNotice(text);
    } finally { submitting.current = false; setBusy(false); }
  };
  return <>
    <section className="discovery-hero"><div className="hero-copy"><span className="store-eyebrow">THE EVERYDAY, UPGRADED / 01</span><h1>Good finds.<br />Full throttle<span>.</span></h1><p>A fresh way to discover what you need.<br />Explore the collection. Make your next move.</p><a className="shop-cta" href="#collection">Explore collection <span>↗</span></a><div className="hero-footnote"><span>01 / DISCOVER</span><span>02 / CHECKOUT</span><span>03 / MAKE IT YOURS</span></div></div><RallyCar /></section>
    <section id="collection" className="collection-section"><div className="section-heading"><div><span className="section-kicker">CURATED BY YOUR CURIOSITY</span><h2>The collection <span className="muted">({visible.length})</span></h2></div><a className="basket-jump" href="#basket">Your basket · {count} items ↗</a></div>
      {notice && <div className="alert error" role="alert">{notice} <button className="btn ghost small" onClick={load}>Refresh</button></div>}
      <div className="store-filters"><input className="input" aria-label="Search products" placeholder="Find your next favourite…" value={query} onChange={e => setQuery(e.target.value)} /><select className="select" aria-label="Category" value={category} onChange={e => setCategory(e.target.value)}><option value="all">All categories</option>{categories.map(c => <option key={c}>{c}</option>)}</select><select className="select" aria-label="Sort products" value={sort} onChange={e => setSort(e.target.value)}><option value="name">Name: A–Z</option><option value="low">Price: low to high</option><option value="high">Price: high to low</option></select></div>
      <div className="store-content"><div>{loading ? <div className="card">Loading the collection…</div> : !visible.length ? <div className="card empty"><b>No products found</b>Try another search or category.</div> : <div className="store-products">{visible.map((p, index) => <article className="shop-product" key={p.id}><button className={`product-art art-${index % 4}`} onClick={() => setDetail(p)} aria-label={`View ${p.name}`}><span className="product-monogram">{p.name.slice(0, 2).toUpperCase()}</span><span className="art-category">{p.categoryName || 'Collection'}</span><span className="art-arrow">↗</span></button><div className="product-info"><small>{p.quantity > 0 ? 'Available now' : 'Currently sold out'}</small><button className="product-title" onClick={() => setDetail(p)}>{p.name}</button><p>{p.description || 'Discover the details.'}</p><div className="product-bottom"><b>{money(p.price)}</b><div className="quantity-control"><button aria-label={`Remove one ${p.name}`} disabled={busy || !cart[p.id]} onClick={() => quantity(p, cart[p.id] - 1)}>−</button><output>{cart[p.id] || 0}</output><button aria-label={`Add one ${p.name}`} disabled={busy || p.quantity <= (cart[p.id] || 0)} onClick={() => quantity(p, (cart[p.id] || 0) + 1)}>+</button></div></div></div></article>)}</div>}</div>
      <aside id="basket" className="store-basket"><span className="section-kicker">YOUR NEXT GOOD FIND</span><h2>The basket <span>{count}</span></h2>{!selected.length ? <div className="basket-empty"><span>＋</span><b>Room for something good.</b><p>Add a product to get started.</p></div> : selected.map(p => <div className="basket-item" key={p.id}><b>{p.name}</b><span>{money(p.price)}</span><label>Quantity<input aria-label={`Quantity for ${p.name}`} type="number" min="0" max={p.quantity} step="1" disabled={busy} value={cart[p.id]} onChange={e => quantity(p, e.target.value)} /></label><button disabled={busy} onClick={() => quantity(p, 0)}>Remove</button></div>)}<div className="basket-total"><span>Estimated total</span><b>{money(total)}</b></div>{invalid && <p role="alert">Availability changed. Update the quantities in your basket.</p>}<button className="shop-cta" disabled={busy || invalid || !selected.length || total <= 0} onClick={() => setReview(true)}>Review & checkout <span>↗</span></button><p className="basket-note">Test checkout with Razorpay. Final totals are confirmed when your order is created.</p><Link to="/shop/orders">Already ordered? View your orders →</Link></aside></div>
    </section>
    {review && <Modal title="Review your order" busy={busy} onClose={() => setReview(false)}><p>Ordering as <b>{profile.name || profile.phoneNumber}</b></p>{selected.map(p => <div className="summary-line" key={p.id}><span>{p.name} × {cart[p.id]}</span><b>{money(p.price * cart[p.id])}</b></div>)}<div className="basket-total"><span>Estimated total</span><b>{money(total)}</b></div><p className="muted">Razorpay opens next. Use a test payment method; no real money is charged.</p>{notice && <div className="alert error">{notice}</div>}<button className="btn" disabled={busy || invalid} onClick={checkout}>{busy ? 'Preparing payment…' : 'Place order & pay'}</button></Modal>}
    {detail && <Modal title={detail.name} onClose={() => setDetail(null)}><span className="badge">{detail.categoryName || 'Collection'}</span><p>{detail.description || 'No additional description available.'}</p><h2>{money(detail.price)}</h2><p>{detail.quantity > 0 ? `${detail.quantity} available` : 'Currently sold out'}</p><details className="technical"><summary>Product reference</summary><code>{detail.id}</code></details><button className="btn" disabled={busy || (cart[detail.id] || 0) >= detail.quantity} onClick={() => { quantity(detail, (cart[detail.id] || 0) + 1); setDetail(null); }}>Add to basket</button></Modal>}
  </>;
}
