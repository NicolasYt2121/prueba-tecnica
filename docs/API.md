# API REST

Base local: `http://localhost:8080/api`. Content-Type: `application/json`. Fechas ISO. Importes con máximo 17 enteros y 2 decimales. En el frontend se envían como cadena decimal para no perder precisión antes de llegar a BigDecimal.

| Método | Ruta | Resultado |
|---|---|---|
| GET / POST | `/customers` | Listar / crear cliente (201) |
| GET / PUT / DELETE | `/customers/{id}` | Consultar / modificar / eliminar si no tiene productos (204) |
| GET / POST | `/accounts` | Listar / abrir cuenta (201) |
| GET | `/accounts?customerId=1` | Filtrar cuentas del cliente |
| GET / PUT / DELETE | `/accounts/{id}` | Consultar / estado y GMF / cancelar con saldo cero (204) |
| GET | `/accounts/{id}/statement` | Cuenta, saldo, disponible y asientos ordenados |
| GET / POST | `/transactions` | Listar / registrar operación (201) |
| GET / PUT / DELETE | `/transactions/{id}` | Consultar / editar descripción / anular mediante reversión (204) |

## Crear cliente
```json
{"identificationType":"CC","identificationNumber":"10203040","firstName":"Ana","lastName":"Pérez","email":"ana@example.com","birthDate":"1990-05-20"}
```
Identificación: `CC`, `CE`, `PASAPORTE`. PUT requiere todos los campos editables. La API devuelve ID y fechas calculadas.

## Abrir cuenta
```json
{"customerId":1,"type":"AHORROS","gmfExempt":true}
```
Tipos: `AHORROS`, `CORRIENTE`. PUT:
```json
{"status":"INACTIVA","gmfExempt":false}
```
Estados: `ACTIVA`, `INACTIVA`, `CANCELADA`.

## Registrar movimientos
Generar un UUID nuevo para una operación nueva. Conservar el mismo UUID para reintentar esa operación ante un fallo de red.
```json
{"requestId":"ed8e48db-44f5-45b5-8204-50dfc53f3ad6","type":"CONSIGNACION","sourceAccountId":null,"destinationAccountId":1,"amount":"100000.00","description":"Apertura"}
```
```json
{"requestId":"13a210d4-796f-4c72-9527-b514f833f2e8","type":"RETIRO","sourceAccountId":1,"destinationAccountId":null,"amount":"5000.00","description":"Retiro"}
```
```json
{"requestId":"5966bf63-8f4a-4b76-919f-b2e76e6077f1","type":"TRANSFERENCIA","sourceAccountId":1,"destinationAccountId":2,"amount":"25000.00","description":"Transferencia entre clientes"}
```
PUT: `{"description":"Referencia corregida"}`. No modifica importes. DELETE no borra físicamente: la respuesta de consulta incluye `reversed: true` y `reversedAt`.

## Errores

- 400: campos requeridos, email, formato JSON, enum, importe o tipo inválido.
- 404: registro inexistente.
- 409: fondos insuficientes, productos vinculados, cuenta inactiva, cancelación no permitida, identificación duplicada o conflicto concurrente.
- 422: regla de negocio como minoría de edad o combinación de cuentas inválida.

Formato Problem Details con `status`, `detail` y, en validaciones de campos, `fields`. Los detalles de SQL y credenciales no se exponen. Un 409 ante un POST concurrente se resuelve consultando el resultado y reintentando con el mismo UUID.

Salud: `GET /actuator/health`, fuera del prefijo `/api`.
