export const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
export function cents(value) { const s=String(value??'0');const negative=s.startsWith('-');const [whole,fraction='']=s.replace(/^[-+]/,'').split('.');return (BigInt(whole||'0')*100n+BigInt((fraction+'00').slice(0,2)))*(negative?-1n:1n); }
export function money(value) { const c=typeof value==='bigint'?value:cents(value);const abs=c<0n?-c:c;return `${c<0n?'-':''}$ ${new Intl.NumberFormat('es-CO').format(abs/100n)},${String(abs%100n).padStart(2,'0')}`; }
export const date = value => value ? new Intl.DateTimeFormat('es-CO',{dateStyle:'medium',timeStyle:'short',timeZone:'America/Bogota'}).format(new Date(value)) : '—';
export const labels={AHORROS:'Ahorros',CORRIENTE:'Corriente',ACTIVA:'Activa',INACTIVA:'Inactiva',CANCELADA:'Cancelada',CONSIGNACION:'Consignación',RETIRO:'Retiro',TRANSFERENCIA:'Transferencia'};
export function badge(status) { return `<span class="badge ${['ACTIVA','INACTIVA','CANCELADA'].includes(status)?status.toLowerCase():''}"><i></i>${escapeHtml(labels[status]??status)}</span>`; }
export function empty(title,description,action='') { return `<div class="empty"><span class="empty-icon">▧</span><h3>${escapeHtml(title)}</h3><p>${escapeHtml(description)}</p>${action}</div>`; }
export function table(headers,rows) { return `<div class="table-scroll"><table><thead><tr>${headers.map(h=>`<th scope="col">${escapeHtml(h)}</th>`).join('')}</tr></thead><tbody>${rows.join('')}</tbody></table></div>`; }
export function metric(label,value,caption,icon) { return `<article class="metric"><div>${escapeHtml(label)}<span>${icon}</span></div><strong>${escapeHtml(value)}</strong><small>${escapeHtml(caption)}</small></article>`; }
export function field(name,label,type='text',value='',extra='') { return `<label>${escapeHtml(label)}<input name="${name}" type="${type}" value="${escapeHtml(value)}" ${extra}></label>`; }
export function select(name,label,items,value) { return `<label>${escapeHtml(label)}<select name="${name}" required>${items.map(([v,t])=>`<option value="${escapeHtml(v)}" ${String(v)===String(value)?'selected':''}>${escapeHtml(t)}</option>`).join('')}</select></label>`; }
export function csvCell(value) { let s=String(value??''); if(/^[=+\-@\t\r]/.test(s)) s="'"+s; return '"'+s.replaceAll('"','""')+'"'; }
