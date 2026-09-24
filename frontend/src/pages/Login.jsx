import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authApi, friendlyError } from '../lib/api';
import { useAuth } from '../lib/auth';

export default function Login() {
  const [step, setStep] = useState(1);
  const [phoneNumber, setPhoneNumber] = useState('9763369894');
  const [otpCode, setOtpCode] = useState('');
  const [status, setStatus] = useState(null); // { type, text }
  const [busy, setBusy] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const sendOtp = async (e) => {
    e.preventDefault();
    setBusy(true);
    setStatus(null);
    try {
      await authApi.sendOtp(phoneNumber.trim());
      setStep(2);
      setStatus({ type: 'info', text: 'OTP sent. Check your Spring Boot console — the OTP is printed there (OTP::::::::xxxxxx).' });
    } catch (err) {
      setStatus({ type: 'error', text: friendlyError(err, 'Could not send the OTP. Is the backend running?') });
    } finally {
      setBusy(false);
    }
  };

  const verifyOtp = async (e) => {
    e.preventDefault();
    setBusy(true);
    setStatus(null);
    try {
      const res = await authApi.verifyOtp(phoneNumber.trim(), otpCode.trim());
      await login(res.token);
      navigate('/');
    } catch (err) {
      setStatus({ type: 'error', text: friendlyError(err, 'That code didn’t work — check it and try again.') });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-wrap">
      <div className="login-hero">
        <div>
          <div className="brand" style={{ padding: 0, marginBottom: 36 }}>
            <div className="brand-mark">▦</div>
            <div><b>Stockline</b><small>Inventory OS</small></div>
          </div>
          <h1>Inventory,<br />minus the chaos.</h1>
          <p>
            A minimal showcase for a Spring Boot inventory backend — OTP auth with JWT,
            products, categories, stock quantities and role-based admin controls.
          </p>
          <div className="hero-points">
            <div>◷ &nbsp;OTP login → JWT &nbsp;<code>POST /api/v1/auth/send-otp</code></div>
            <div>▦ &nbsp;Products CRUD &nbsp;<code>GET /api/v1/products</code></div>
            <div>◍ &nbsp;Categories (admin) &nbsp;<code>/api/v1/categorie</code></div>
          </div>
        </div>
        <p style={{ fontSize: 12 }}>Spring Boot · Postgres · Redis OTP · JWT · React showcase</p>
      </div>

      <div className="login-form-side">
        <div className="login-card">
          <div className="steps"><i className="on" /><i className={step === 2 ? 'on' : ''} /></div>
          <h2 style={{ margin: '0 0 6px', letterSpacing: '-0.02em', fontSize: 26 }}>
            {step === 1 ? 'Sign in with phone' : 'Enter your OTP'}
          </h2>
          <p style={{ color: '#6b7280', fontSize: 14, marginTop: 0 }}>
            {step === 1
              ? 'We use passwordless OTP, backed by Redis with a 5-minute expiry.'
              : `Code sent to ${phoneNumber}. Demo: read it from backend logs.`}
          </p>

          {status && <div className={`alert ${status.type}`}>{status.text}</div>}

          {step === 1 ? (
            <form onSubmit={sendOtp}>
              <label className="label">Phone number</label>
              <input
                className="input"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="9763369894"
                inputMode="tel"
                required
              />
              <button className="btn" style={{ width: '100%', marginTop: 14 }} disabled={busy}>
                {busy ? 'Sending…' : 'Send OTP →'}
              </button>
              <p className="footer-note">
                Admin demo account: <span className="kbd">9763369894</span> (seeded by DataInitializer).
                New numbers auto-register as USER.
              </p>
            </form>
          ) : (
            <form onSubmit={verifyOtp}>
              <label className="label">6-digit OTP</label>
              <input
                className="input otp-input"
                value={otpCode}
                onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                placeholder="••••••"
                inputMode="numeric"
                required
              />
              <button className="btn" style={{ width: '100%', marginTop: 14 }} disabled={busy}>
                {busy ? 'Verifying…' : 'Verify & enter →'}
              </button>
              <button type="button" className="btn ghost" style={{ width: '100%', marginTop: 8 }} onClick={() => setStep(1)}>
                ← Change number
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
