import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { usersApi } from './api';

const AuthContext = createContext(null);

function decodeRole(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || payload.roles || null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem('stockline.token'));
  const [profile, setProfile] = useState(null);
  const [loadingProfile, setLoadingProfile] = useState(false);

  const role = useMemo(() => (token ? decodeRole(token) : null), [token]);
  const isAdmin = role === 'ADMIN';

  const refreshProfile = useCallback(async () => {
    if (!localStorage.getItem('stockline.token')) return null;
    setLoadingProfile(true);
    try {
      const me = await usersApi.me();
      setProfile(me);
      return me;
    } catch {
      return null;
    } finally {
      setLoadingProfile(false);
    }
  }, []);

  useEffect(() => {
    if (token) refreshProfile();
    else setProfile(null);
  }, [token, refreshProfile]);

  const login = useCallback(
    async (newToken) => {
      localStorage.setItem('stockline.token', newToken);
      setToken(newToken);
      await refreshProfile();
    },
    [refreshProfile],
  );

  const logout = useCallback(() => {
    localStorage.removeItem('stockline.token');
    setToken(null);
    setProfile(null);
  }, []);

  const value = useMemo(
    () => ({ token, role, isAdmin, profile, loadingProfile, login, logout, refreshProfile }),
    [token, role, isAdmin, profile, loadingProfile, login, logout, refreshProfile],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside AuthProvider');
  return ctx;
}
