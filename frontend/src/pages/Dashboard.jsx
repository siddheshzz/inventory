import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { categoriesApi, friendlyError, productsApi } from '../lib/api';
import { useAuth } from '../lib/auth';

function Stat({ label, value, hint }) {
  return (
    <div className="card stat">
      <small>{label}</small>
      <strong>{value}</strong>
      <span>{hint}</span>
    </div>
  );
}

export default function Dashboard() {
  const { profile, isAdmin } = useAuth();
  const [products, setProducts] = useState([]);
  const [catCount, setCatCount] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const list = await productsApi.list();
        setProducts(Array.isArray(list) ? list : []);
        if (isAdmin) {
          try {
            const cats = await categoriesApi.list();
            setCatCount(Array.isArray(cats) ? cats.length : 0);
          } catch { setCatCount(null); }
        }
      } catch (e) { setError(friendlyError(e, 'Could not load products.')); }
    })();
  }, [isAdmin]);

  const stats = useMemo(() => {
    const totalUnits = products.reduce((s, p) => s + (p.quantity || 0), 0);
    const value = products.reduce((s, p) => s + (Number(p.price) || 0) * (p.quantity || 0), 0);
    const low = products.filter((p) => (p.quantity || 0) <= 5).length;
    const inactive = products.filter((p) => p.active === false).length;
    return { totalUnits, value, low, inactive };
  }, [products]);

  const lowStock = useMemo(
    () => [...products].sort((a, b) => (a.quantity || 0) - (b.quantity || 0)).slice(0, 5),
    [products],
  );

  return (
    <>
      <div className="page-head">
        <div>
          <h1>Good day{profile?.name ? `, ${profile.name.split(' ')[0]}` : ''}.</h1>
          <p>Live view of your Spring Boot inventory — stock levels, value and products needing attention.</p>
        </div>
        <Link to="/products"><button className="btn">Manage products →</button></Link>
      </div>

      {error && <div className="alert error">Could not load products: {error}</div>}

      <div className="grid-4">
        <Stat label="Products" value={products.length} hint="tracked SKUs" />
        <Stat label="Units in stock" value={stats.totalUnits} hint="sum of quantity" />
        <Stat label="Stock value" value={`₹${stats.value.toLocaleString('en-IN')}`} hint="price × quantity" />
        <Stat label="Low stock" value={stats.low} hint="≤ 5 units · needs restock" />
      </div>

      <div className="grid-2" style={{ marginTop: 14 }}>
        <div className="card">
          <h3 style={{ margin: '0 0 4px' }}>Needs attention</h3>
          <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}>Lowest quantities first</p>
          {lowStock.length === 0 ? (
            <div className="empty"><b>No products yet</b>Add your first product to see it here.</div>
          ) : (
            <div className="table-wrap" style={{ boxShadow: 'none' }}>
              <table>
                <thead><tr><th>Product</th><th>Qty</th><th>Price</th></tr></thead>
                <tbody>
                  {lowStock.map((p) => (
                    <tr key={p.id}>
                      <td><b>{p.name}</b></td>
                      <td>
                        <span className={`badge ${(p.quantity || 0) <= 5 ? 'amber' : 'green'}`}>
                          {p.quantity ?? 0} units
                        </span>
                      </td>
                      <td className="mono">₹{p.price}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="card">
          <h3 style={{ margin: '0 0 4px' }}>System snapshot</h3>
          <p style={{ margin: '0 0 14px', color: '#6b7280', fontSize: 13 }}>What this demo wires up</p>
          <div style={{ display: 'grid', gap: 10, fontSize: 14 }}>
            <div>◷ <b>Auth</b> — phone OTP (Redis, 5 min) → JWT <span className="kbd">role: {isAdmin ? 'ADMIN' : 'USER'}</span></div>
            <div>▦ <b>Products</b> — {products.length} live from <span className="kbd">GET /products</span></div>
            <div>◍ <b>Categories</b> — {catCount === null ? (isAdmin ? 'could not load' : 'admin-only endpoint') : `${catCount} categories`}</div>
            <div>◈ <b>Inventory</b> — stock verify + restock under the Inventory tab (admin)</div>
            <div>⛔ <b>Inactive</b> — {stats.inactive} products flagged inactive</div>
          </div>
          <div className="alert info" style={{ marginTop: 16, marginBottom: 0 }}>
            Orders are live — place one from the Orders tab. Each order decrements stock and writes <span className="kbd">SALE</span> transactions.
          </div>
        </div>
      </div>
    </>
  );
}
