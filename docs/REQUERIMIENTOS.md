# Requisitos recibidos en el chat

El enunciado fue proporcionado como texto. No se recibió el PDF original; este archivo es una síntesis de trazabilidad, no una copia del PDF.

| Requisito | Implementación |
|---|---|
| Java backend y capas entity/service/controller/repository | Spring Boot, paquetes en `com.nexo.banking` |
| MVC o hexagonal | Hexagonal: núcleo Java, puertos de entrada/salida y adaptadores REST/JPA; ARQUITECTURA-HEXAGONAL.md |
| PostgreSQL, MySQL, SQL Server u Oracle | PostgreSQL 16 |
| Cliente CRUD con identificación, nombres, correo, nacimiento y fechas | CustomerService + CustomerController |
| Mayoría de edad, creación automática, modificación automática | Validación Java, trigger SQL, Clock |
| No eliminar cliente con productos | Bloqueo, regla de servicio, FK RESTRICT |
| Email válido, nombres/apellidos mínimo dos caracteres | Bean Validation, trim y CHECK SQL |
| Solo ahorro/corriente; producto vinculado a cliente | Enum y CHECK, FK NOT NULL |
| Número único de 10 dígitos; prefijos 53/33 | Secuencia PostgreSQL, generador y constraints |
| Estados, exención GMF, fechas, saldo, titular | Account, DTO y migración |
| Ahorro activo al crear y saldo no negativo | Servicio + CHECK SQL |
| Activar/inactivar; cancelar solo con cero | AccountService y CHECK SQL |
| Consignación/retiro/transferencia | TransactionService |
| Actualizar saldo y disponible | BigDecimal + Unit of Work + CHECK de igualdad |
| Transferencia entre cuentas existentes, crédito/débito | FK, bloqueos ordenados, dos movimientos atómicos |
| CRUD transacciones | Crear/consultar/editar nota/anular; decisión contable documentada |
| Estados de cuenta | Endpoint y pantalla con CSV |
| Pruebas JUnit Service y Controller | Mockito + MockMvc + JaCoCo, umbral de líneas 80% por paquete |
| Prueba con componentes | Componentes frontend + pruebas Node; integración Spring con PostgreSQL real |
| Prueba dockerizada | Dockerfile multi-stage, Compose y servicio de pruebas |
| SOLID, patrones, ACID, DML/DDL | ARQUITECTURA.md, DML.sql, migración Flyway |
| Servicios nube usados | Ninguno desplegado; GitHub Actions preparado |
| Git/GitHub, ramas, commits/push | Historial local por etapas; publicación requiere remoto y autenticación |

