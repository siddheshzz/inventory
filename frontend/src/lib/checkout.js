import { paymentApi } from './api';

let sdk;
export function loadCheckout() {
  if (window.Razorpay) return Promise.resolve();
  if (!sdk) sdk = new Promise((resolve, reject) => {
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    const fail = () => { clearTimeout(timer); script.remove(); sdk = null; reject(new Error('Payment window could not load. Check your connection and retry.')); };
    const timer = setTimeout(fail, 20000);
    script.onload = () => { clearTimeout(timer); if (window.Razorpay) resolve(); else fail(); };
    script.onerror = fail;
    document.head.appendChild(script);
  });
  return sdk;
}

export async function prepareCheckout() {
  const [config] = await Promise.all([paymentApi.config(), loadCheckout()]);
  if (!config.testMode || !config.keyId?.startsWith('rzp_test_')) throw new Error('Test payments are not configured. Ask the administrator to configure Razorpay test keys.');
  return config;
}

// The server alone decides whether the signed gateway payment is captured and paid.
export async function payOrder(order, profile, config) {
  config ||= await prepareCheckout();
  const payment = await paymentApi.checkout(order.id);
  return new Promise((resolve, reject) => {
    let verifying = false;
    const checkout = new window.Razorpay({
      key: config.keyId, order_id: payment.razorpayOrderId,
      amount: payment.amountInPaise, currency: payment.currency,
      name: 'Stockline', description: `Order ${order.id.slice(0, 8)}`,
      prefill: { name: profile?.name || '', email: profile?.email || '', contact: profile?.phoneNumber || '' },
      theme: { color: '#d9fb52' },
      modal: { ondismiss: () => { if (!verifying) resolve({ paid: false }); } },
      handler: async response => {
        verifying = true;
        const proof = { razorpayOrderId: response.razorpay_order_id, razorpayPaymentId: response.razorpay_payment_id, razorpaySignature: response.razorpay_signature };
        // Keep signed confirmation for retry if the network or capture is delayed.
        sessionStorage.setItem(`stockline.payment.${order.id}`, JSON.stringify(proof));
        try { await confirmSavedPayment(order.id); resolve({ paid: true }); }
        catch { reject(new Error('Payment received; confirmation is pending. Use “Verify payment” in My orders before paying again.')); }
      },
    });
    checkout.on('payment.failed', () => { /* Razorpay presents retry and dismissal inside its window. */ });
    checkout.open();
  });
}

export async function confirmSavedPayment(id) {
  const proof = JSON.parse(sessionStorage.getItem(`stockline.payment.${id}`));
  if (!proof) throw new Error('No payment confirmation is waiting.');
  await paymentApi.confirm(id, proof);
  sessionStorage.removeItem(`stockline.payment.${id}`);
}
