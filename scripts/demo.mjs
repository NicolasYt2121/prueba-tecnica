// Solo datos ficticios. Requiere la aplicación local iniciada.
import {randomUUID} from 'node:crypto';
const base=process.env.APP_URL??'http://localhost:8080';
async function api(path,body) {
  const r=await fetch(base+'/api'+path,{method:body?'POST':'GET',headers:body?{'Content-Type':'application/json'}:{},body:body?JSON.stringify(body):undefined});
  const data=await r.json();if(!r.ok)throw new Error(data.detail??String(r.status));return data;
}
const existing=await api('/customers');
if(existing.some(c=>c.identificationNumber==='DEMO1001')) {console.log('La demostración ya está cargada.');process.exit(0);}
const ana=await api('/customers',{identificationType:'CC',identificationNumber:'DEMO1001',firstName:'Ana María',lastName:'Pérez · Demo',email:'ana.demo@example.com',birthDate:'1992-06-15'});
const carlos=await api('/customers',{identificationType:'CC',identificationNumber:'DEMO1002',firstName:'Carlos',lastName:'Ramírez · Demo',email:'carlos.demo@example.com',birthDate:'1987-11-03'});
const ahorro=await api('/accounts',{customerId:ana.id,type:'AHORROS',gmfExempt:true});
const corriente=await api('/accounts',{customerId:carlos.id,type:'CORRIENTE',gmfExempt:false});
for(const [type,sourceAccountId,destinationAccountId,amount,description] of [
  ['CONSIGNACION',null,ahorro.id,'2500000.00','Consignación de demostración'],
  ['CONSIGNACION',null,corriente.id,'1200000.00','Apertura de demostración'],
  ['TRANSFERENCIA',ahorro.id,corriente.id,'350000.00','Transferencia de demostración'],
  ['RETIRO',corriente.id,null,'150000.00','Retiro de demostración']
]) await api('/transactions',{requestId:randomUUID(),type,sourceAccountId,destinationAccountId,amount,description});
console.log('Datos ficticios cargados: 2 clientes, 2 cuentas y 4 movimientos.');
