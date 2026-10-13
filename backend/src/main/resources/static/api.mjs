export async function request(path,method='GET',body) {
  let response;
  try { response=await fetch('/api'+path,{method,headers:body?{'Content-Type':'application/json'}:{},body:body?JSON.stringify(body):undefined}); }
  catch { throw new Error('No fue posible conectar. Verifica la conexión y consulta el estado antes de reintentar.'); }
  if(response.status===204) return null;
  const data=await response.json();
  if(!response.ok) throw new Error((data.detail??'No fue posible completar la operación')+(data.fields?' · '+Object.keys(data.fields).join(', '):''));
  return data;
}
