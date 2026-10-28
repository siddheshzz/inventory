import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../lib/auth';

export default function CustomerLayout({ children }) {
  const { profile, logout, isAdmin } = useAuth();
  return <div className="customer-shell">
    <div className="store-strip">A little discovery. A lot of possibility. <span>Razorpay test payments · No real charges</span></div>
    <header className="store-header"><Link className="store-brand" to="/shop"><span>S↗</span> stockline<span className="brand-dot">.</span></Link>
      <nav aria-label="Customer navigation"><NavLink to="/shop" end>Discover</NavLink><NavLink to="/shop/orders">My orders</NavLink><NavLink to="/shop/account">My account</NavLink></nav>
      <div className="store-user"><span>{profile?.name || 'Welcome'}</span>{isAdmin && <Link to="/admin">Admin ↗</Link>}<button onClick={logout}>Sign out</button></div>
    </header>
    <main className="store-main">{children}</main>
    <footer className="store-footer"><b>stockline.</b><span>Find it. Make it yours.</span><Link to="/shop/account">Manage your profile & addresses ↗</Link></footer>
  </div>;
}
