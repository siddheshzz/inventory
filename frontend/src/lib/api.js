// Thin fetch wrapper around the Spring Boot API.
//
// Backend base URL: same-origin via Vite proxy in dev (`/api/...` -> :8080),
// or VITE_API_URL in production. Endpoints (from controllers):
//   POST /api/v1/auth/send-otp   { phoneNumber }
//   POST /api/v1/auth/verify-otp { phoneNumber, otpCode } -> { token, expiresIn }
//   GET  /api/v1/products, GET /api/v1/products/{id}
//   POST /api/v1/products (ADMIN), PATCH /api/v1/products/{id} (ADMIN), DELETE (ADMIN)
//   GET/POST /api/v1/categorie, PATCH/DELETE /api/v1/categorie/{id} (all ADMIN, note singular path)
//   GET /api/v1/user/me, PATCH /api/v1/user/me
//   GET /api/v1/user, GET/PUT/DELETE /api/v1/user/{id} (ADMIN)
//   GET /api/v1/orders (ADMIN)
//   POST /api/v1/orders?userId={uuid} (USER,ADMIN) { items:[{productId, quantity}], discount? }
//     -> 201 OrderResponse { id, status, subtotal, discount, tax, shippingCharges,
//        grandTotal, paymentStatus, userId, items:[{id, productId, productName,
//        quantity, unitPrice, discount, tax, lineTotal}], createdAt, updatedAt }
//   NOTE: shippingAddressId is accepted but ignored server-side (no address flow yet).

const RAW_BASE = import.meta.env.VITE_API_URL || '';
const BASE = RAW_BASE.replace(/\/$/, '');

function url(path) {
  return `${BASE}${path}`;
}

function authHeaders() {
  const token = localStorage.getItem('stockline.token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function api(path, { method = 'GET', body, auth = true } = {}) {
  const res = await fetch(url(path), {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(auth ? authHeaders() : {}),
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  });

  if (res.status === 204) return null;

  let data = null;
  try {
    data = await res.json();
  } catch {
    data = null;
  }

  if (!res.ok) {
    const message =
      (data && (data.message || data.error || data.detail)) ||
      `Request failed (${res.status})`;
    const err = new Error(message);
    err.status = res.status;
    err.data = data;
    throw err;
  }
  return data;
}

export const authApi = {
  sendOtp: (phoneNumber) =>
    api('/api/v1/auth/send-otp', { method: 'POST', body: { phoneNumber }, auth: false }),
  verifyOtp: (phoneNumber, otpCode) =>
    api('/api/v1/auth/verify-otp', { method: 'POST', body: { phoneNumber, otpCode }, auth: false }),
};

export const productsApi = {
  list: () => api('/api/v1/products'),
  get: (id) => api(`/api/v1/products/${id}`),
  create: (payload) => api('/api/v1/products', { method: 'POST', body: payload }),
  update: (id, payload) => api(`/api/v1/products/${id}`, { method: 'PATCH', body: payload }),
  remove: (id) => api(`/api/v1/products/${id}`, { method: 'DELETE' }),
};

export const categoriesApi = {
  list: () => api('/api/v1/categorie'),
  create: (payload) => api('/api/v1/categorie', { method: 'POST', body: payload }),
  update: (id, payload) => api(`/api/v1/categorie/${id}`, { method: 'PATCH', body: payload }),
  remove: (id) => api(`/api/v1/categorie/${id}`, { method: 'DELETE' }),
};

export const usersApi = {
  me: () => api('/api/v1/user/me'),
  updateMe: (payload) => api('/api/v1/user/me', { method: 'PATCH', body: payload }),
  list: () => api('/api/v1/user'),
  updateById: (id, payload) => api(`/api/v1/user/${id}`, { method: 'PUT', body: payload }),
  removeById: (id) => api(`/api/v1/user/${id}`, { method: 'DELETE' }),
};

export const ordersApi = {
  list: () => api('/api/v1/orders'),
  get: (id) => api(`/api/v1/orders/${id}`),
  create: (userId, payload) =>
    api(`/api/v1/orders?userId=${userId}`, { method: 'POST', body: payload }),
  // POST /api/v1/orders/{id}/items (USER,ADMIN) { productId, quantity }
  // PENDING orders only; decrements stock + SALE tx. -> 201 updated order.
  addItem: (orderId, payload) =>
    api(`/api/v1/orders/${orderId}/items`, { method: 'POST', body: payload }),
  // DELETE /api/v1/orders/{id}/items/{itemId} (USER,ADMIN)
  // PENDING only, not the last line; restores stock + RETURN tx.
  removeItem: (orderId, itemId) =>
    api(`/api/v1/orders/${orderId}/items/${itemId}`, { method: 'DELETE' }),
  // PATCH /api/v1/orders/{id}/status?status=X (ADMIN)
  // CANCELLED restores all stock + RETURN txs. Terminal orders reject changes.
  updateStatus: (orderId, status) =>
    api(`/api/v1/orders/${orderId}/status?status=${status}`, { method: 'PATCH' }),
};

export const inventoryApi = {
  // GET /api/v1/inventory/products/{id}/stock-verify (ADMIN)
  // -> { productId, cachedQuantity, ledgerQuantity, match }
  verifyStock: (productId) => api(`/api/v1/inventory/products/${productId}/stock-verify`),
  // POST /api/v1/inventory/purchase?adminUserId={uuid} (ADMIN)
  // body { productId, quantity, reference? } -> 201 StockVerifyResponse
  purchase: (adminUserId, payload) =>
    api(`/api/v1/inventory/purchase?adminUserId=${adminUserId}`, { method: 'POST', body: payload }),
};

// ---------------------------------------------------------------------------
// Error presentation: raw backend text (SQL, constraint names, stack traces)
// must never reach the UI. Route every user-facing error through
// friendlyError(err, fallback).
// ---------------------------------------------------------------------------

// Matches backend leakage: SQLSTATE dumps, constraint names, table
// operations, Hibernate/Spring internals.
const TECH_LEAK = /foreign key|violates|constraint|psql|hibernate|jdbc|syntax error|sql\s*\[|delete from|insert into|update\s+\w+\s+set|dataintegrity|stacktrace|\bat\s+java\.|org\.(springframework|hibernate|apache|postgresql)/i;

export function isForeignKeyError(err) {
  const text = `${err?.message || ''} ${err?.data?.message || ''}`;
  return /foreign key|violates.*constraint/i.test(text);
}

function looksClean(text) {
  return (
    typeof text === 'string' &&
    text.length > 0 &&
    text.length < 200 &&
    !TECH_LEAK.test(text)
  );
}

export function friendlyError(err, fallback) {
  if (err?.status === 401) return 'Your session expired — please sign in again.';
  if (err?.status === 403) return 'You do not have permission for this action (admin only).';
  // Clean business messages (e.g. "Not enough stock…", "Invalid OTP")
  // are safe to show as-is.
  const raw = err?.data?.message || err?.message || '';
  if (looksClean(raw)) return raw;
  return fallback || 'Something went wrong on the server. Please try again.';
}
