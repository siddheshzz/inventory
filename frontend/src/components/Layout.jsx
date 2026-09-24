import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/auth';

export default function Layout({ children }) {
  const { profile, role, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">▦</div>
          <div>
            <b>Stockline</b>
            <small>Inventory OS</small>
          </div>
        </div>

        <nav className="nav">
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            Overview
          </NavLink>
          <NavLink to="/products" className={({ isActive }) => (isActive ? 'active' : '')}>
            Products
          </NavLink>
          <NavLink to="/categories" className={({ isActive }) => (isActive ? 'active' : '')}>
            Categories {isAdmin ? '' : <span className="count">admin</span>}
          </NavLink>
          <NavLink to="/orders" className={({ isActive }) => (isActive ? 'active' : '')}>
            Orders
          </NavLink>
          <NavLink to="/inventory" className={({ isActive }) => (isActive ? 'active' : '')}>
            Inventory {isAdmin ? '' : <span className="count">admin</span>}
          </NavLink>
          <NavLink to="/users" className={({ isActive }) => (isActive ? 'active' : '')}>
            Team & Profile
          </NavLink>
        </nav>

        <div className="side-foot">
          <div className="user-chip">
            <b>{profile?.name || 'Signed in'}</b>
            <span>{profile?.phoneNumber || ''}</span>
            <br />
            <span className={`role-pill ${role === 'ADMIN' ? '' : 'user'}`}>
              {role || 'USER'}
            </span>
          </div>
          <button className="btn ghost" style={{ borderColor: '#333', color: '#fff' }} onClick={handleLogout}>
            Sign out
          </button>
        </div>
      </aside>

      <main className="main">{children}</main>
    </div>
  );
}
