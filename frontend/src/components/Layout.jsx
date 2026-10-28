import { NavLink, Link } from 'react-router-dom';
import { useAuth } from '../lib/auth';

const links = [['', 'Overview'], ['/orders', 'Customer orders'], ['/products', 'Products'], ['/categories', 'Categories'], ['/inventory', 'Stock & restocking'], ['/ledger', 'Stock ledger'], ['/team', 'Team & customers'], ['/account', 'My account']];
export default function Layout({ children }) {
  const { profile, logout } = useAuth();
  return <div className="shell admin-shell">
    <aside className="sidebar">
      <Link to="/admin" className="brand"><span className="brand-mark">S</span><span><b>Stockline</b><small>Operations workspace</small></span></Link>
      <span className="nav-caption">MANAGE YOUR BUSINESS</span>
      <nav className="nav" aria-label="Administration">{links.map(([path, label]) => <NavLink key={path} to={`/admin${path}`} end={!path}>{label}</NavLink>)}</nav>
      <div className="side-foot"><Link className="store-link" to="/shop">Open customer storefront ↗</Link><div className="user-chip"><b>{profile?.name || profile?.phoneNumber || 'Administrator'}</b><span>Administrator</span></div><button className="btn signout" onClick={logout}>Sign out</button></div>
    </aside>
    <main className="main">{children}</main>
  </div>;
}
