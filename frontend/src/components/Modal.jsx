import { useEffect, useRef } from 'react';

export default function Modal({ title, onClose, children, busy = false }) {
  const ref = useRef(null);
  useEffect(() => { const dialog = ref.current; dialog.showModal(); return () => dialog.close(); }, []);
  return <dialog ref={ref} className="form-dialog" onCancel={e => { e.preventDefault(); if (!busy) onClose(); }}>
    <div className="dialog-title"><h2>{title}</h2><button className="btn ghost" aria-label="Close dialog" disabled={busy} onClick={onClose}>×</button></div>{children}
  </dialog>;
}
