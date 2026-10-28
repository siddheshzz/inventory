import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ordersApi, friendlyError } from '../lib/api';
import { useAuth } from '../lib/auth';
import { dateTime, money, titleCase, newestFirst } from '../lib/presentation';
import { payOrder, confirmSavedPayment } from '../lib/checkout';
import OrderDetails from '../components/OrderDetails';

export default function CustomerOrders() {
  const { profile } = useAuth();
  const { state } = useLocation();
  const [orders, setOrders] = useState([]), [loading, setLoading] = useState(true), [error, setError] = useState(null);
  const [notice, setNotice] = useState(state?.message), [expanded, setExpanded] = useState(state?.orderId), [busy, setBusy] = useState(null);
  const load = async () => { setError(null); try { setOrders((await ordersApi.mine()).sort(newestFirst)); } catch (e) { setError(friendlyError(e, 'Could not load your orders.')); } finally { setLoading(false); } };
  useEffect(() => { load(); }, []);
  const pay = async order => {
    if (busy) return; setBusy(order.id); setNotice(null);
    try { if (sessionStorage.getItem(`stockline.payment.${order.id}`)) { await confirmSavedPayment(order.id); setNotice('Payment verified. Your order is paid.'); } else { const result = await payOrder(order, profile); setNotice(result.paid ? 'Payment verified. Thank you!' : 'Payment closed. Your order is saved and can be paid later.'); } await load(); }
    catch (e) { setNotice(friendlyError(e, 'Could not complete payment.')); } finally { setBusy(null); }
  };
  return <><div className="page-head"><div><span className="section-kicker">EVERY PURCHASE, IN ONE PLACE</span><h1>My orders</h1><p>Your purchases, payment status and progress. Newest first.</p></div><Link className="btn" to="/shop">Continue shopping ↗</Link></div>
    {notice && <div role="status" className="alert info">{notice}</div>}{error && <div role="alert" className="alert error">{error} <button className="btn ghost small" onClick={load}>Retry</button></div>}
    {loading ? <div className="card">Loading your orders…</div> : !orders.length && !error ? <div className="card empty"><b>Your first good find is waiting.</b><p>You haven’t placed an order yet.</p><Link to="/shop">Explore the collection →</Link></div> : <div className="customer-orders">{orders.map(o => <article className="purchase-card" key={o.id}><div className="purchase-top"><div><small>PLACED ON</small><b>{dateTime(o.createdAt)}</b></div><div><small>TOTAL</small><b>{money(o.grandTotal)}</b></div><div><small>PAYMENT</small><span className={`badge ${o.paymentStatus === 'CONFIRMED' ? 'green' : 'amber'}`}>{o.paymentStatus === 'CONFIRMED' ? 'Paid' : titleCase(o.paymentStatus)}</span></div><span className="badge">{titleCase(o.status)}</span></div><div className="purchase-content"><div><h3>{(o.items || []).map(i => i.productName).join(', ') || 'Order'}</h3><p>{(o.items || []).reduce((sum, i) => sum + i.quantity, 0)} items · Reference #{o.id.slice(0, 8)}</p></div><div className="button-row">{o.status === 'PENDING' && o.paymentStatus === 'PENDING' && <button className="btn" disabled={!!busy} onClick={() => pay(o)}>{busy === o.id ? 'Processing…' : sessionStorage.getItem(`stockline.payment.${o.id}`) ? 'Verify payment' : 'Pay with Razorpay'}</button>}<button className="btn ghost" aria-expanded={expanded === o.id} onClick={() => setExpanded(expanded === o.id ? null : o.id)}>{expanded === o.id ? 'Hide details' : 'View order'}</button></div></div>{expanded === o.id && <OrderDetails order={o} />}</article>)}</div>}
  </>;
}
