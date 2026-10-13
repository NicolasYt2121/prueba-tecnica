import {escapeHtml as e,money,cents,date,labels,badge,empty,table,metric,field,select,csvCell} from './components.mjs';
import {request} from './api.mjs';
const state={view:'inicio',customers:[],accounts:[],transactions:[],search:''};
const main=document.querySelector('#main'), dialog=document.querySelector('#editor'), form=document.querySelector('#editor-form');
let saveAction,toastTimer;
const customerName=id=>{const c=state.customers.find(c=>c.id===id);return c?c.firstName+' '+c.lastName:'Cliente #'+id;};
const accountNumber=id=>state.accounts.find(a=>a.id===id)?.number??'—';
const action=(name,label,id,cls='')=>`<button class="text-button ${cls}" data-action="${name}" data-id="${id??''}">${label}</button>`;
const createButton=(name,label)=>`<button class="button primary" data-action="${name}"><span>＋</span> ${label}</button>`;
function toast(message) { const el=document.querySelector('#toast');el.textContent=message;el.classList.add('visible');clearTimeout(toastTimer);toastTimer=setTimeout(()=>el.classList.remove('visible'),4500); }
async function refresh() {
  const [customers,accounts,transactions]=await Promise.all([request('/customers'),request('/accounts'),request('/transactions')]);
  Object.assign(state,{customers,accounts,transactions});document.querySelector('#connection').textContent='● Sistema conectado'; render();
}
function heading(kicker,title,description,button='') { return `<div class="page-heading"><div><span class="eyebrow">${kicker}</span><h1>${title}</h1><p>${description}</p></div>${button}</div>`; }
function search(placeholder) { return `<div class="toolbar"><div class="search"><span>⌕</span><input id="search" aria-label="Buscar" placeholder="${placeholder}" value="${e(state.search)}"></div><span class="subtle">Actualizado en tiempo real al operar</span></div>`; }
function customerRows(items) { return items.map(c=>`<tr><td><div class="person"><span class="avatar small">${e((c.firstName[0]??'')+(c.lastName[0]??''))}</span><div><strong>${e(c.firstName+' '+c.lastName)}</strong><small>Cliente #${c.id}</small></div></div></td><td>${e(c.identificationType)} ${e(c.identificationNumber)}</td><td>${e(c.email)}</td><td>${e(c.birthDate)}</td><td class="actions">${action('edit-customer','Editar',c.id)}${action('delete-customer','Eliminar',c.id,'danger')}</td></tr>`); }
function transactionRows(items) { return items.map(t=>`<tr><td><strong>${e(labels[t.type])}</strong><small>#${t.id} · ${e(t.description||'Sin descripción')}</small></td><td class="mono">${e(accountNumber(t.sourceAccountId))}<small>→ ${e(accountNumber(t.destinationAccountId))}</small></td><td class="amount">${money(t.amount)}</td><td>${date(t.createdAt)}</td><td>${t.reversed?'<span class="badge cancelada">Anulada</span>':'<span class="badge activa">Completada</span>'}</td><td class="actions">${t.reversed?'—':action('edit-transaction','Nota',t.id)+action('delete-transaction','Anular',t.id,'danger')}</td></tr>`); }
function render() {
  const q=state.search.toLocaleLowerCase('es');
  document.querySelectorAll('[data-view]').forEach(b=>b.classList.toggle('active',b.dataset.view===state.view));
  document.querySelector('#breadcrumb').textContent={inicio:'Vista general',clientes:'Clientes',cuentas:'Productos',movimientos:'Movimientos'}[state.view];
  if(state.view==='inicio') {
    const balance=state.accounts.reduce((sum,a)=>sum+cents(a.balance),0n);
    main.innerHTML=heading('TU OPERACIÓN, EN UN SOLO LUGAR','Cada movimiento cuenta.','Administra clientes y productos con una visión clara de tu operación.',createButton('new-transaction','Nuevo movimiento'))+
    `<div class="metrics">${metric('Clientes registrados',state.customers.length,'Relaciones que construyes','♧')}${metric('Productos activos',state.accounts.filter(a=>a.status==='ACTIVA').length,state.accounts.length+' cuentas en total','▤')}${metric('Saldo consolidado',money(balance),'Suma de saldos · COP','↗')}${metric('Movimientos',state.transactions.filter(t=>!t.reversed).length,'Operaciones vigentes','⇄')}</div>`+
    `<div class="overview-grid"><section class="panel"><div class="panel-heading"><h2>Actividad reciente</h2><button class="text-button" data-view="movimientos">Ver todos →</button></div>${state.transactions.length?table(['Operación','Cuentas','Importe','Fecha','Estado',''],transactionRows(state.transactions.slice(0,5))):empty('Tu primera operación empieza aquí','Registra un cliente, abre una cuenta y realiza un movimiento.')}</section><aside class="quick-card"><span class="eyebrow">UN BUEN COMIENZO</span><h2>Más cerca de<br>tus clientes.</h2><p>Crea su perfil y acompaña cada paso de su vida financiera.</p><button class="button light" data-action="new-customer">Registrar cliente <span>↗</span></button><div class="abstract-art" aria-hidden="true"><i></i><i></i><i></i></div></aside></div>`+
    `<section class="note"><span>ⓘ</span><div><strong>Un historial que conserva cada detalle</strong><p>Las transferencias registran débito y crédito. Al anular, se crean movimientos inversos para mantener la trazabilidad.</p></div></section>`;
  } else if(state.view==='clientes') {
    const items=state.customers.filter(c=>`${c.firstName} ${c.lastName} ${c.identificationNumber} ${c.email}`.toLocaleLowerCase('es').includes(q));
    main.innerHTML=heading('PERSONAS Y RELACIONES','Clientes','Gestiona la información de las personas que confían en tu entidad.',createButton('new-customer','Registrar cliente'))+`<section class="panel">${search('Buscar por nombre, identificación o correo')}<div class="panel-heading"><h2>Directorio de clientes <span class="count">${items.length}</span></h2></div>${items.length?table(['Cliente','Identificación','Correo electrónico','Nacimiento','Acciones'],customerRows(items)):empty('No hay clientes para mostrar','Registra un cliente o prueba otra búsqueda.')}</section>`;
  } else if(state.view==='cuentas') {
    const items=state.accounts.filter(a=>`${a.number} ${customerName(a.customerId)}`.toLocaleLowerCase('es').includes(q));
    main.innerHTML=heading('PRODUCTOS FINANCIEROS','Cuentas','Consulta saldos, administra estados y revisa el detalle de cada cuenta.',createButton('new-account','Abrir cuenta'))+`<section class="panel">${search('Buscar por número de cuenta o cliente')}<div class="panel-heading"><h2>Portafolio de productos <span class="count">${items.length}</span></h2></div>${items.length?table(['Cuenta','Titular','Estado','Saldo / disponible','GMF','Acciones'],items.map(a=>`<tr><td><strong class="mono">${a.number}</strong><small>${labels[a.type]}</small></td><td>${e(customerName(a.customerId))}</td><td>${badge(a.status)}</td><td class="amount">${money(a.balance)}<small>Disponible ${money(a.availableBalance)}</small></td><td>${a.gmfExempt?'Exenta':'No exenta'}</td><td class="actions">${action('statement','Estado de cuenta',a.id)}${action('edit-account','Editar',a.id)}${a.status!=='CANCELADA'?action('delete-account','Cancelar',a.id,'danger'):''}</td></tr>`)):empty('Tu portafolio está por comenzar','Abre una cuenta de ahorros o corriente para un cliente registrado.')}</section>`;
  } else {
    const items=state.transactions.filter(t=>`${labels[t.type]} ${t.description} ${accountNumber(t.sourceAccountId)} ${accountNumber(t.destinationAccountId)}`.toLocaleLowerCase('es').includes(q));
    main.innerHTML=heading('TRAZABILIDAD DE LA OPERACIÓN','Movimientos','Consignaciones, retiros y transferencias entre las cuentas de la entidad.',createButton('new-transaction','Nuevo movimiento'))+`<section class="panel">${search('Buscar por tipo, cuenta o descripción')}<div class="panel-heading"><h2>Historial de transacciones <span class="count">${items.length}</span></h2></div>${items.length?table(['Operación','Origen / destino','Importe','Fecha','Estado','Acciones'],transactionRows(items)):empty('Aún no hay movimientos','Las operaciones exitosas aparecerán aquí con su estado y fecha.')}</section>`;
  }
}
function openForm(title,html,onSave) {
  document.querySelector('#dialog-title').textContent=title;document.querySelector('#form-fields').innerHTML=html;document.querySelector('#form-error').textContent='';document.querySelector('.dialog-actions').hidden=false;saveAction=onSave;form.reset();dialog.showModal();
}
function customerForm(c) {
  openForm(c?'Editar cliente':'Registrar cliente',`<div class="form-grid">${select('identificationType','Tipo de identificación',[['CC','Cédula de ciudadanía'],['CE','Cédula de extranjería'],['PASAPORTE','Pasaporte']],c?.identificationType)}${field('identificationNumber','Número de identificación','text',c?.identificationNumber,'required pattern="[A-Za-z0-9-]{4,25}" minlength="4" maxlength="25"')}${field('firstName','Nombres','text',c?.firstName,'required minlength="2" maxlength="80"')}${field('lastName','Apellidos','text',c?.lastName,'required minlength="2" maxlength="80"')}${field('email','Correo electrónico','email',c?.email,'required maxlength="160"')}${field('birthDate','Fecha de nacimiento','date',c?.birthDate,'required')}</div><p class="form-help">Solo se permite registrar mayores de edad. Las fechas de creación y modificación se calculan automáticamente.</p>`,data=>request('/customers'+(c?'/'+c.id:''),c?'PUT':'POST',data));
}
function accountForm(a) {
  if(!a&&!state.customers.length) { toast('Primero registra un cliente.');return; }
  const html=a?select('status','Estado de la cuenta',Object.entries(labels).filter(([k])=>['ACTIVA','INACTIVA','CANCELADA'].includes(k)),a.status):select('customerId','Titular',state.customers.map(c=>[c.id,c.firstName+' '+c.lastName+' · '+c.identificationNumber]))+select('type','Tipo de producto',[['AHORROS','Cuenta de ahorros'],['CORRIENTE','Cuenta corriente']]);
  openForm(a?'Administrar cuenta '+a.number:'Abrir cuenta',html+`<label class="checkbox"><input type="checkbox" name="gmfExempt" ${a?.gmfExempt?'checked':''}> Exenta de GMF</label><p class="form-help">${a?'Solo puedes cancelar cuentas con saldo cero. La cancelación es definitiva.':'El número se genera automáticamente. La cuenta inicia activa y con saldo cero.'}</p>`,data=>{data.gmfExempt=data.gmfExempt==='on';if(!a)data.customerId=Number(data.customerId);return request('/accounts'+(a?'/'+a.id:''),a?'PUT':'POST',data);});
}
function transactionForm() {
  const active=state.accounts.filter(a=>a.status==='ACTIVA');if(!active.length) {toast('Abre o activa una cuenta para registrar movimientos.');return;}
  const items=active.map(a=>[a.id,a.number+' · '+customerName(a.customerId)]);const requestId=crypto.randomUUID();
  openForm('Nuevo movimiento',select('type','Tipo de operación',[['CONSIGNACION','Consignación'],['RETIRO','Retiro'],['TRANSFERENCIA','Transferencia']])+`<div id="source-field">${select('sourceAccountId','Cuenta de origen',items)}</div><div id="destination-field">${select('destinationAccountId','Cuenta de destino',items)}</div>`+field('amount','Importe (COP)','number','','required min="0.01" step="0.01" max="99999999999999999.99"')+field('description','Descripción','text','','maxlength="250"')+'<p class="form-help">Los saldos se actualizan únicamente si toda la operación es exitosa.</p>',data=>request('/transactions','POST',{...data,requestId,sourceAccountId:data.type==='CONSIGNACION'?null:Number(data.sourceAccountId),destinationAccountId:data.type==='RETIRO'?null:Number(data.destinationAccountId),amount:data.amount}));
  const type=form.elements.type;const update=()=>{for(const [id,show] of [['source-field',type.value!=='CONSIGNACION'],['destination-field',type.value!=='RETIRO']]) { const el=document.getElementById(id);el.hidden=!show;el.querySelector('select').disabled=!show; }};type.addEventListener('change',update);update();
}
async function statement(id) {
  const s=await request('/accounts/'+id+'/statement');
  openForm('Estado de cuenta · '+s.account.number,`<div class="statement-summary"><span>${e(customerName(s.account.customerId))}</span><strong>${money(s.account.balance)}</strong><small>Saldo disponible: ${money(s.account.availableBalance)}</small></div>${s.movements.length?table(['Fecha','Movimiento','Débito / crédito','Saldo'],s.movements.map(m=>`<tr><td>${date(m.createdAt)}</td><td>#${m.transactionId}${m.reversal?' · Reversión':''}</td><td class="${Number(m.delta)<0?'negative':'positive'}">${money(m.delta)}</td><td>${money(m.balanceAfter)}</td></tr>`)):empty('Sin movimientos','La cuenta aún no tiene operaciones.')}<button type="button" id="download-csv" class="button secondary">Descargar CSV</button>`,null);
  document.querySelector('.dialog-actions').hidden=true;
  document.querySelector('#download-csv').onclick=()=>{const rows=[['Fecha','Transacción','Importe','Saldo','Reversión'],...s.movements.map(m=>[m.createdAt,m.transactionId,m.delta,m.balanceAfter,m.reversal])];const blob=new Blob(['\uFEFF'+rows.map(r=>r.map(csvCell).join(';')).join('\r\n')],{type:'text/csv;charset=utf-8'});const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download='estado-'+s.account.number+'.csv';a.click();setTimeout(()=>URL.revokeObjectURL(url),1000);};
}
document.addEventListener('click',async event=>{
  const nav=event.target.closest('[data-view]');if(nav) {state.view=nav.dataset.view;state.search='';render();return;}
  const button=event.target.closest('[data-action]');if(!button)return;const id=Number(button.dataset.id);
  try {switch(button.dataset.action) {
    case 'new-customer':customerForm();break;
    case 'edit-customer':customerForm(state.customers.find(c=>c.id===id));break;
    case 'new-account':accountForm();break;
    case 'edit-account':accountForm(state.accounts.find(a=>a.id===id));break;
    case 'new-transaction':transactionForm();break;
    case 'edit-transaction':{const t=state.transactions.find(t=>t.id===id);openForm('Editar descripción',field('description','Descripción','text',t.description,'maxlength="250"')+'<p class="form-help">El importe y las cuentas se conservan. Para corregirlos, anula y registra una nueva operación.</p>',data=>request('/transactions/'+id,'PUT',data));break;}
    case 'statement':await statement(id);break;
    default:{const config={'delete-customer':['customers','¿Eliminar este cliente? Solo es posible si no tiene productos.'],'delete-account':['accounts','¿Cancelar esta cuenta? Debe tener saldo cero y no podrá reabrirse.'],'delete-transaction':['transactions','¿Anular este movimiento? Se revertirán sus saldos conservando el historial.']}[button.dataset.action];if(config&&confirm(config[1])) {button.disabled=true;await request('/'+config[0]+'/'+id,'DELETE');await refresh();toast('Operación completada.');} }
  }}catch(error) {toast(error.message);}finally{button.disabled=false;}
});
main.addEventListener('input',event=>{if(event.target.id!=='search')return;const start=event.target.selectionStart;state.search=event.target.value;render();const input=document.querySelector('#search');input.focus();input.setSelectionRange(start,start);});
form.addEventListener('submit',async event=>{event.preventDefault();if(!saveAction)return;const button=document.querySelector('#save-button');button.disabled=true;button.textContent='Guardando…';document.querySelector('#form-error').textContent='';try{await saveAction(Object.fromEntries(new FormData(form)));dialog.close();toast('Guardado correctamente.');try{await refresh();}catch{toast('Guardado. No se pudo actualizar la vista; recarga para consultar.');}}catch(error){document.querySelector('#form-error').textContent=error.message;}finally{button.disabled=false;button.textContent='Guardar';}});
document.querySelector('#close-dialog').onclick=()=>dialog.close();document.querySelector('#cancel-dialog').onclick=()=>dialog.close();
refresh().catch(error=>{document.querySelector('#connection').textContent='● Sin conexión';main.innerHTML=empty('No pudimos cargar la información',error.message,'<button class="button primary" id="retry">Reintentar</button>');document.querySelector('#retry').onclick=()=>location.reload();});
