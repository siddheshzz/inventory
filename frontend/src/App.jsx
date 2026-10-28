import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import CustomerLayout from './components/CustomerLayout';
import { useAuth } from './lib/auth';
import Categories from './pages/Categories';
import Dashboard from './pages/Dashboard';
import Inventory from './pages/Inventory';
import Login from './pages/Login';
import Orders from './pages/Orders';
import Products from './pages/Products';
import Users from './pages/Users';
import Catalogue from './pages/Catalogue';
import Ledger from './pages/Ledger';
import Account from './pages/Account';
import CustomerOrders from './pages/CustomerOrders';

function Protected({ admin = false, children }) {
  const { token, isAdmin, profile, loadingProfile, profileError, refreshProfile, logout } = useAuth();
  if (!token) return <Navigate to="/login" replace />;
  if (!profile && loadingProfile) return <div className="card">Loading your account…</div>;
  if (!profile && profileError) return <div className="card"><p>{profileError}</p><button className="btn" onClick={refreshProfile}>Retry</button> <button className="btn ghost" onClick={logout}>Sign out</button></div>;
  if (admin && !isAdmin) return <Navigate to="/shop" replace />;
  return admin ? <Layout>{children}</Layout> : <CustomerLayout>{children}</CustomerLayout>;
}

export default function App() {
  const { token, isAdmin } = useAuth();
  const home = isAdmin ? '/admin' : '/shop';
  return <Routes>
    <Route path="/login" element={token ? <Navigate to={home} replace /> : <Login />} />
    <Route path="/" element={<Navigate to={token ? home : '/login'} replace />} />
    <Route path="/shop" element={<Protected><Catalogue /></Protected>} />
    <Route path="/shop/orders" element={<Protected><CustomerOrders /></Protected>} />
    <Route path="/shop/account" element={<Protected><Account /></Protected>} />
    <Route path="/admin" element={<Protected admin><Dashboard /></Protected>} />
    <Route path="/admin/products" element={<Protected admin><Products /></Protected>} />
    <Route path="/admin/categories" element={<Protected admin><Categories /></Protected>} />
    <Route path="/admin/orders" element={<Protected admin><Orders /></Protected>} />
    <Route path="/admin/inventory" element={<Protected admin><Inventory /></Protected>} />
    <Route path="/admin/ledger" element={<Protected admin><Ledger /></Protected>} />
    <Route path="/admin/team" element={<Protected admin><Users /></Protected>} />
    <Route path="/admin/account" element={<Protected admin><Account /></Protected>} />
    {['products', 'categories', 'inventory', 'ledger'].map(path => <Route key={path} path={`/${path}`} element={<Navigate to={`/admin/${path}`} replace />} />)}
    <Route path="/orders" element={<Navigate to={isAdmin ? '/admin/orders' : '/shop/orders'} replace />} />
    <Route path="/users" element={<Navigate to={isAdmin ? '/admin/team' : '/shop/account'} replace />} />
    <Route path="/catalogue" element={<Navigate to="/shop" replace />} />
    <Route path="*" element={<Navigate to={home} replace />} />
  </Routes>;
}
