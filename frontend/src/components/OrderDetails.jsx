import { customerName, dateTime, money, titleCase } from '../lib/presentation';
const stages = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];
export default function OrderDetails({ order }) {
  return <div className="order-details">
    <div className="order-facts"><div><small>Ordered by</small><b>{customerName(order)}</b><span>{order.userPhone}</span></div><div><small>Placed on</small><b>{dateTime(order.createdAt)}</b></div><div><small>Last updated</small><b>{dateTime(order.updatedAt)}</b></div></div>
    {order.status !== 'CANCELLED' ? <ol className="order-progress" aria-label="Order progress">{stages.map(s => <li key={s} className={stages.indexOf(s) <= stages.indexOf(order.status) ? 'reached' : ''} aria-current={s === order.status ? 'step' : undefined}>{titleCase(s)}</li>)}</ol> : <div className="alert info">This order was cancelled.</div>}
    <div className="table-wrap"><table><thead><tr><th>Product</th><th>Quantity</th><th>Unit price</th><th>Line total</th></tr></thead><tbody>{(order.items || []).map(i => <tr key={i.id}><td><b>{i.productName || 'Product name unavailable'}</b></td><td>{i.quantity}</td><td>{money(i.unitPrice)}</td><td>{money(i.lineTotal)}</td></tr>)}</tbody></table></div>
    <div className="order-breakdown"><div><span>Subtotal</span><b>{money(order.subtotal)}</b></div><div><span>Discount</span><b>−{money(order.discount)}</b></div><div><span>Tax</span><b>{money(order.tax)}</b></div><div><span>Shipping</span><b>{money(order.shippingCharges)}</b></div><div><span>Total</span><strong>{money(order.grandTotal)}</strong></div></div>
    <details className="technical"><summary>Order references</summary><p>Order UUID: <code>{order.id}</code><br />Customer UUID: <code>{order.userId}</code></p>{(order.items || []).map(i => <p key={i.id}>{i.productName}: <code>{i.productId}</code></p>)}</details>
  </div>;
}
