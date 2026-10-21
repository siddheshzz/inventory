# Stockline — Inventory frontend (Vite + React)

Minimal, showcase-worthy UI for the Spring Boot inventory backend in the repo root.

## Run

```bash
# 1. Start backend + Postgres + Redis
docker-compose up --build
# backend on http://localhost:8080

# 2. Start frontend
cd frontend
npm install
npm run dev
# app on http://localhost:5173
```

Dev proxy forwards `/api/*` → `http://localhost:8080`, so no CORS setup is needed
locally. For production builds, set `VITE_API_URL`:

```bash
VITE_API_URL=https://api.example.com npm run build
```

## Login

Passwordless OTP, matching `AuthController`:

1. Enter phone number → `POST /api/v1/auth/send-otp`
2. Read the 6-digit OTP from the **backend console** (`OTP::::::::xxxxxx` — SMS not wired yet)
3. Enter OTP → `POST /api/v1/auth/verify-otp` → JWT stored in `localStorage`

- Admin demo: `9763369894` (seeded by `DataInitializer`, role `ADMIN`)
- Any new number auto-registers as `USER`

## What maps to what

| Page | Backend |
|---|---|
| Overview | `GET /api/v1/products` (stats computed client-side) |
| Products | `GET/POST/PATCH /api/v1/products` (writes = ADMIN); `DELETE` soft-deletes (active=false), blocked by open orders, one-way (no reactivate) |
| Categories | `GET/POST/PATCH/DELETE /api/v1/categorie` — note singular path, all ADMIN |
| Orders | `POST /orders` (owner = JWT principal, `Idempotency-Key` per submission — retries return the original, changed body → 409); PENDING lines editable via `POST/DELETE /orders/{id}/items[/{itemId}]`; `PATCH /orders/{id}/status?status=` (ADMIN, CANCELLED restores stock, terminal locked, non-owners get "Not your order") |
| Inventory | verify cached vs ledger; `POST /purchase` restocks (actor = JWT); `POST /products/{id}/stock-reconcile` writes the balancing ADJUSTMENT |
| Team & Profile | `GET/PATCH /api/v1/user/me`, `GET /api/v1/user` (ADMIN list has a backend mapping quirk — error is surfaced in UI) |

JWT role claim (`role`) is decoded client-side to gate admin UI.
