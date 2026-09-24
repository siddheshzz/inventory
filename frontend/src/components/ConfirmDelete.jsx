import { useState } from 'react';

/**
 * Guided delete confirmation. Explains the precondition up front:
 * records with linked history cannot be deleted, and what to do instead.
 *
 * Props:
 *   title        — modal heading, e.g. "Delete product?"
 *   itemName     — the record being deleted
 *   requirements — list of strings: what must be true for deletion to succeed
 *   blockedHint  — string: what to do when deletion is blocked
 *   onCancel, onConfirm, busy
 */
export default function ConfirmDelete({
  title,
  itemName,
  sub = null, // custom subtitle node; defaults to the deletion warning
  ackLabel = 'I understand this is permanent, and that linked history will block the deletion.',
  requirements = [],
  blockedHint,
  confirmLabel = 'Delete',
  busy = false,
  onCancel,
  onConfirm,
}) {
  const [ack, setAck] = useState(false);

  return (
    <div className="modal-backdrop" onClick={onCancel}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>{title}</h3>
        <p className="sub">
          {sub || (<><b>{itemName}</b> — deletion is permanent and cannot be undone.</>)}
        </p>

        <div
          style={{
            border: '1px solid #e9e9e9', borderRadius: 12, padding: '12px 14px',
            fontSize: 13, lineHeight: 1.6, background: '#fcfcfc', marginBottom: 12,
          }}
        >
          <b>Before you delete, all of these must be true:</b>
          <ul style={{ margin: '8px 0 0', paddingLeft: 18 }}>
            {requirements.map((r) => (
              <li key={r} style={{ marginBottom: 4 }}>{r}</li>
            ))}
          </ul>
        </div>

        {blockedHint && (
          <div className="alert info" style={{ marginBottom: 12 }}>
            {blockedHint}
          </div>
        )}

        <label style={{ display: 'flex', gap: 8, alignItems: 'flex-start', fontSize: 13, cursor: 'pointer' }}>
          <input
            type="checkbox"
            checked={ack}
            onChange={(e) => setAck(e.target.checked)}
            style={{ marginTop: 3 }}
          />
          {ackLabel}
        </label>
        <div className="row-end">
          <button type="button" className="btn ghost" onClick={onCancel}>Keep it</button>
          <button
            type="button"
            className="btn danger"
            disabled={busy || !ack}
            onClick={onConfirm}
          >
            {busy ? 'Working…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
