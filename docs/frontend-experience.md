# Customer and admin experience

The customer storefront lives under `/shop`: discover products, build a basket,
review the order, pay, see saved orders, and manage personal details and addresses.
Administration lives under `/admin`: customer orders, products, categories, stock,
the stock ledger, team/customer accounts and the administrator's own profile.
Customers are redirected away from admin routes; server method permissions remain
the authority. Customer history is selected by the authenticated user's ID.

## Razorpay test checkout

Keep `razorpay.key-id` and `razorpay.key-secret` configured on the Spring backend.
Use matching **test-mode** keys. `/api/v1/payments/config` returns only the public
test key ID, never the secret. The storefront refuses to open a live-key checkout.
No frontend environment key is necessary. Restart/rebuild the backend after these
API additions; a previously running Docker image does not contain the new endpoints.

The browser loads Razorpay's hosted Checkout script, creates the local order with
an idempotency key, requests its gateway order, opens Checkout, then submits the
signed callback to the existing confirmation endpoint. The backend checks the
signature, amount and captured payment. Closing Checkout leaves the order unpaid;
the customer can resume it from My orders without creating a second order.
If confirmation fails after the gateway callback, the signed proof stays in session
storage so “Verify payment” can retry confirmation without charging again.

Use the test payment options shown by Razorpay Checkout. Test card details:
https://razorpay.com/docs/payments/payments/test-card-details/
Official integration reference:
https://razorpay.com/docs/payments/payment-gateway/web-integration/standard/integration-steps/

## Scope and existing domain rules

Address book entries are persisted, editable, owner-scoped and support a default.
They are **not a shipping-address snapshot on an order**: the existing order model
has no shipping-address relationship. Checkout does not claim an address was attached.
Adding shipping and immutable address snapshots is a separate domain decision.

Stock is deducted when orders are created, following the existing service. A pending
checkout blocks item/status changes. Paid cancellations require a refund-aware flow
which the existing backend deliberately rejects. The UI reflects these restrictions.
Team members use the existing ADMIN/USER roles and OTP sign-in. There is no separate
staff role. Phone numbers remain immutable sign-in identities. Deletion is blocked
for accounts with order or stock history and for the caller's own account.

The car is a locally generated Three.js scene; no downloaded vehicle model is used.
It loads separately from the application bundle, pauses when hidden, honours reduced
motion on load, and has an explicit animation pause control. Product tiles use
typographic artwork because the product model has no image field.

## Verification

From `frontend/`: `npm run build`, `npm run lint`, `npx playwright test`.
Browser tests mock API responses and Razorpay callbacks; they verify UI contracts,
role separation, payment success/dismissal, account forms, team and stock actions.
They do not claim a real gateway transaction or database integration succeeded.
From the project root: `./mvnw -Dtest=CustomerExperienceTests test` checks ownership,
default-address handling and protection of account history using mocked repositories.
