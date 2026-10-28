import { test, expect } from '@playwright/test';

const products = [
  { id: 'product-1', name: 'Trail Backpack', description: 'Room for your everyday adventures.', price: 2400, quantity: 8, active: true, categoryName: 'Outdoor', categoryId: 'cat-1' },
  { id: 'product-2', name: 'Studio Headphones', description: 'Find your focus. Hear every detail.', price: 3600, quantity: 2, active: true, categoryName: 'Electronics', categoryId: 'cat-2' },
  { id: 'product-3', name: 'Field Notebook', description: 'For the ideas worth keeping.', price: 450, quantity: 0, active: true, categoryName: 'Essentials', categoryId: 'cat-3' },
  { id: 'product-4', name: 'Desk Lamp', description: 'Light up your next great idea.', price: 1800, quantity: 6, active: true, categoryName: 'Home', categoryId: 'cat-4' },
  { id: 'product-5', name: 'Travel Flask', description: 'Take a little comfort with you.', price: 850, quantity: 12, active: true, categoryName: 'Outdoor', categoryId: 'cat-1' },
  { id: 'product-6', name: 'Canvas Tote', description: 'A dependable everyday companion.', price: 700, quantity: 10, active: true, categoryName: 'Essentials', categoryId: 'cat-3' },
];
const baseOrder = { id: 'a1111111-1111-1111-1111-111111111111', userId: 'user-1', userName: 'Aarav Shah', userPhone: '9999999999', status: 'PENDING', paymentStatus: 'PENDING', subtotal: 2400, grandTotal: 2400, tax: 0, discount: 0, shippingCharges: 0, createdAt: '2026-10-27T14:30:00', updatedAt: '2026-10-27T14:30:00', items: [{ id: 'item-1', productId: 'product-1', productName: 'Trail Backpack', quantity: 1, unitPrice: 2400, lineTotal: 2400 }] };

async function fixture(page, role = 'USER', payment = 'success') {
  const requests = [];
  let profile = { id: 'user-1', name: 'Aarav Shah', email: 'aarav@example.test', phoneNumber: '9999999999', role };
  let addresses = [], orders = [{ ...baseOrder }], members = [profile];
  await page.addInitScript(role => localStorage.setItem('stockline.token', `test.${btoa(JSON.stringify({ role }))}.signature`), role);
  await page.route('https://fonts.googleapis.com/**', route => route.abort());
  await page.route('https://checkout.razorpay.com/v1/checkout.js', route => route.fulfill({ contentType: 'application/javascript', body: `window.Razorpay = class { constructor(options) { this.options=options; } on(){} open() { window.__checkoutOptions=this.options; ${payment === 'dismiss' ? 'this.options.modal.ondismiss();' : 'this.options.handler({razorpay_order_id:"order_test_1",razorpay_payment_id:"pay_test_1",razorpay_signature:"signed_test_response"});'} } };` }));
  await page.route('**/api/v1/**', async route => {
    const req = route.request(), path = new URL(req.url()).pathname, method = req.method();
    const body = method === 'POST' || method === 'PUT' || method === 'PATCH' ? req.postDataJSON() : null;
    requests.push({ path, method, body, key: req.headers()['idempotency-key'] });
    let data;
    if (path === '/api/v1/user/me') { if (body) profile = { ...profile, ...body }; data = profile; }
    else if (path === '/api/v1/user') { if (method === 'POST') members.push({ ...body, id: 'new-member' }); data = method === 'POST' ? members.at(-1) : members; }
    else if (path.startsWith('/api/v1/user/')) { if (method === 'DELETE') members = members.filter(m => !path.endsWith(m.id)); else members = members.map(m => path.endsWith(m.id) ? { ...m, ...body } : m); data = {}; }
    else if (path === '/api/v1/products') data = products;
    else if (path === '/api/v1/categorie') data = [{ id: 'cat-1', name: 'Outdoor' }];
    else if (path === '/api/v1/addresses') { if (body) addresses.push({ ...body, id: 'address-1' }); data = body ? addresses.at(-1) : addresses; }
    else if (path.startsWith('/api/v1/addresses/')) { if (method === 'PUT') addresses = [{ ...body, id: 'address-1' }]; else addresses = []; data = addresses[0] || {}; }
    else if (path === '/api/v1/orders/mine') data = orders;
    else if (path === '/api/v1/orders') { if (method === 'POST') { orders = [{ ...baseOrder, createdAt: '2026-10-28T12:00:00' }]; data = orders[0]; } else data = [...orders, { ...baseOrder, id: 'older', userName: 'Meera Rao', createdAt: '2026-10-25T10:00:00' }]; }
    else if (path.endsWith('/status')) { orders[0].status = new URL(req.url()).searchParams.get('status'); data = orders[0]; }
    else if (path === '/api/v1/payments/config') data = { keyId: 'rzp_test_fixture', testMode: true };
    else if (path.endsWith('/checkout')) data = { orderId: baseOrder.id, razorpayOrderId: 'order_test_1', amountInPaise: 240000, currency: 'INR' };
    else if (path.endsWith('/confirm')) { orders[0].paymentStatus = 'CONFIRMED'; data = { paymentStatus: 'CONFIRMED' }; }
    else if (path.endsWith('/stock-verify')) data = { cachedQuantity: 8, ledgerQuantity: 8, match: true };
    else if (path.endsWith('/purchase')) data = { cachedQuantity: 12, ledgerQuantity: 12, match: true };
    else if (path.endsWith('/ledger')) data = [{ id: 'movement-1', productId: 'product-1', productName: 'Trail Backpack', quantityChange: 8, type: 'PURCHASE', createdByName: 'Aarav Shah', createdAt: '2026-10-27T12:00:00', reference: 'Invoice INV-42' }];
    else throw new Error(`Unexpected API request: ${method} ${path}`);
    await route.fulfill({ json: data });
  });
  return requests;
}

test('customer gets a separate storefront, 3D scene and guarded routes', async ({ page }) => {
  const reqs = await fixture(page);
  await page.goto('/'); await expect(page).toHaveURL(/\/shop$/);
  await expect(page.getByRole('navigation', { name: 'Administration' })).toHaveCount(0);
  await expect(page.getByRole('link', { name: 'Stock ledger' })).toHaveCount(0);
  await expect(page.locator('.rally-canvas canvas')).toBeVisible();
  await page.getByRole('button', { name: 'Pause motion' }).click();
  await expect(page.getByRole('button', { name: 'Play motion' })).toBeVisible();
  await page.screenshot({ path: 'test-results/store-desktop.png', fullPage: true });
  await page.goto('/admin/inventory'); await expect(page).toHaveURL(/\/shop$/);
  expect(reqs.some(r => r.path.includes('/inventory'))).toBe(false);
  await page.setViewportSize({ width: 390, height: 844 });
  await expect(page.locator('.rally-canvas canvas[data-rendered="true"]')).toBeVisible();
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(() => requestAnimationFrame(resolve))));
  await page.screenshot({ path: 'test-results/store-mobile.png', fullPage: true });
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true);
});

test('checkout uses gateway callbacks and confirms payment without signature inputs', async ({ page }) => {
  const reqs = await fixture(page);
  await page.goto('/shop');
  await page.getByRole('button', { name: 'Add one Trail Backpack', exact: true }).click();
  await page.getByRole('button', { name: 'Review & checkout' }).click();
  await page.getByRole('button', { name: 'Place order & pay' }).click();
  await expect(page).toHaveURL(/\/shop\/orders$/);
  await expect(page.getByText('Payment verified. Thanks for your order!')).toBeVisible();
  expect(reqs.filter(r => r.path === '/api/v1/orders' && r.method === 'POST')).toHaveLength(1);
  expect(reqs.find(r => r.path === '/api/v1/orders' && r.method === 'POST').key).toBeTruthy();
  expect(reqs.find(r => r.path.endsWith('/confirm')).body.razorpaySignature).toBe('signed_test_response');
  await page.reload(); await expect(page.getByText('Trail Backpack').first()).toBeVisible();
});

test('dismissed checkout resumes payment on the saved order, without duplicate orders', async ({ page }) => {
  const reqs = await fixture(page, 'USER', 'dismiss'); await page.goto('/shop');
  await page.getByRole('button', { name: 'Add one Trail Backpack', exact: true }).click();
  await page.getByRole('button', { name: 'Review & checkout' }).click();
  await page.getByRole('button', { name: 'Place order & pay' }).click();
  await expect(page).toHaveURL(/\/shop\/orders$/);
  await page.getByRole('button', { name: 'Pay with Razorpay' }).click();
  await expect(page.getByText('Payment closed. Your order is saved and can be paid later.')).toBeVisible();
  expect(reqs.filter(r => r.path === '/api/v1/orders' && r.method === 'POST')).toHaveLength(1);
});

test('profile and address create, edit and remove persist through API calls', async ({ page }) => {
  const reqs = await fixture(page); await page.goto('/shop/account');
  await page.getByLabel('Full name').fill('Aarav Patel'); await page.getByRole('button', { name: 'Save details' }).click();
  await expect(page.getByText('Your profile is up to date.')).toBeVisible();
  await page.getByRole('button', { name: '+ Add address' }).click();
  for (const [label, value] of [['Street address', '12 Park Road'], ['City', 'Pune'], ['State / region', 'Maharashtra'], ['Postal code', '411001']]) await page.getByLabel(label, { exact: true }).fill(value);
  await page.getByLabel('Use as my default address').check();
  await page.getByRole('button', { name: 'Save address', exact: true }).click();
  await expect(page.getByText('12 Park Road')).toBeVisible();
  await page.getByRole('button', { name: 'Edit address' }).click();
  await page.getByLabel('Street address', { exact: true }).fill('24 Park Road');
  await page.getByRole('button', { name: 'Save address', exact: true }).click();
  await expect(page.getByText('24 Park Road')).toBeVisible();
  await page.getByRole('button', { name: 'Remove', exact: true }).click();
  await page.getByRole('button', { name: 'Remove address', exact: true }).click();
  await expect(page.getByText('A place to start')).toBeVisible();
  expect(reqs.some(r => r.method === 'PUT' && r.path.includes('/addresses/'))).toBe(true);
});

test('admin sees names and date order, manages team and restocks', async ({ page }) => {
  const reqs = await fixture(page, 'ADMIN'); await page.goto('/admin/orders');
  const rows = page.locator('.admin-order-row'); await expect(rows).toHaveCount(2);
  await expect(rows.first()).toContainText('Aarav Shah'); await expect(rows.nth(1)).toContainText('Meera Rao');
  await page.screenshot({ path: 'test-results/admin-orders.png', fullPage: true });
  await rows.first().click(); await page.getByRole('button', { name: 'Confirm order', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Start processing' })).toBeVisible();
  await page.goto('/admin/team'); await page.getByRole('button', { name: '+ Add member' }).click();
  await page.getByLabel('Full name').fill('Nisha Patil'); await page.getByLabel('Phone number', { exact: true }).fill('9888888888');
  await page.getByRole('button', { name: 'Save member' }).click(); await expect(page.getByText('Nisha Patil', { exact: true })).toBeVisible();
  const memberRow = page.getByRole('row').filter({ hasText: 'Nisha Patil' });
  await memberRow.getByRole('button', { name: 'Edit', exact: true }).click(); await page.getByLabel('Full name').fill('Nisha Rao');
  await page.getByRole('button', { name: 'Save member' }).click();
  await page.getByRole('row').filter({ hasText: 'Nisha Rao' }).getByRole('button', { name: 'Remove' }).click();
  await page.getByRole('button', { name: 'Remove member', exact: true }).click(); await expect(page.getByText('Nisha Rao', { exact: true })).toHaveCount(0);
  await page.goto('/admin/inventory'); await page.getByRole('row').filter({ hasText: 'Trail Backpack' }).getByRole('button', { name: 'Receive stock' }).click();
  await page.getByLabel('Units received').fill('4'); await page.getByRole('button', { name: 'Confirm receipt' }).click();
  await expect(page.getByText('Trail Backpack: 4 units received. The ledger has been updated.')).toBeVisible();
  expect(reqs.find(r => r.path.endsWith('/purchase')).body.quantity).toBe(4);
});
