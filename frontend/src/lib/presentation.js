export const money = value => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(Number(value || 0));
export const dateTime = value => value && !Number.isNaN(Date.parse(value)) ? new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : 'Date unavailable';
export const titleCase = value => String(value || 'Unknown').toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, c => c.toUpperCase());
export const customerName = order => order.userName?.trim() || order.userPhone || 'Name not provided';
export const newestFirst = (a, b) => (Date.parse(b.createdAt) || 0) - (Date.parse(a.createdAt) || 0) || String(b.id).localeCompare(String(a.id));
