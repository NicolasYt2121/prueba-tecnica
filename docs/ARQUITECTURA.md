# Arquitectura y decisiones de diseÃ±o

## Arquitectura elegida

Monolito con arquitectura hexagonal: dominio y casos de uso Java independientes de Spring, JPA y HTTP. Las capas `entity`, `service`, `controller` y `repository` se conservan por requisito, con responsabilidades explícitas. La guía detallada está en [ARQUITECTURA-HEXAGONAL.md](ARQUITECTURA-HEXAGONAL.md).

```mermaid
flowchart LR
  UI[Frontend] --> REST[Controller y DTO]
  REST --> IN[Puertos de entrada]
  S[Servicios de aplicación] -. implementan .-> IN
  S --> D[Modelos de dominio]
  S --> OUT[Puertos de salida]
  JPA[Adaptadores JPA] -. implementan .-> OUT
  JPA --> E[Entidades JPA]
  E --> DB[(PostgreSQL)]
  CFG[Configuración y decoradores transaccionales] --> S
```

## Patrones aplicados

| Patrón | Implementación y propósito |
|---|---|
| Puertos y adaptadores | Interfaces de entrada/salida del núcleo; REST y JPA en los extremos |
| Repository | Contratos de persistencia independientes de Spring Data |
| Dependency Injection | Configuración construye servicios con puertos y reloj |
| Adapter y Mapper | `Jpa*Adapter` traduce entre dominio y entidades persistentes |
| DTO | Entrada HTTP validada convertida explícitamente a comandos Java |
| Decorator y Unit of Work | `Transactional*UseCases` envuelve operaciones completas en transacciones |
| Idempotencia | UUID único evita aplicar dos veces una petición repetida |
| Reversión compensatoria | Anulación con asientos inversos sin borrar evidencia contable |

## SOLID en el código

- **S:** controladores traducen HTTP; servicios deciden reglas; adaptadores persisten y mapean.
- **O:** un nuevo adaptador de almacenamiento o generador implementa los puertos existentes.
- **L:** adaptadores y dobles de prueba cumplen los mismos contratos; los bloqueos requieren una transacción activa.
- **I:** puertos separados por caso de uso y responsabilidad de persistencia, sin exponer todo JpaRepository al núcleo.
- **D:** las interfaces pertenecen al núcleo; Spring y PostgreSQL dependen de ellas. El núcleo compila solamente con el JDK.

## ACID y concurrencia

- **Atomicidad:** consignaciÃ³n, retiro, transferencia y reversiÃ³n ejecutan dentro de los decoradores `@Transactional`. Si una validaciÃ³n o escritura falla, se revierte todo, incluso una transacciÃ³n insertada antes de validar el saldo.
- **Consistencia:** claves primarias/forÃ¡neas, unicidad, CHECK, trigger de mayorÃ­a de edad y validaciones de negocio. `NUMERIC(19,2)` / `BigDecimal` para dinero. No se calcula dinero con `double` en el backend.
- **Aislamiento:** `SELECT FOR UPDATE` mediante JPA bloquea cuentas; una transferencia las bloquea por ID ascendente para reducir interbloqueos. La comprobaciÃ³n de saldo usa el saldo bloqueado. El estado de cuenta usa `REPEATABLE_READ` para leer saldo y movimientos de una misma instantÃ¡nea.
- **Durabilidad:** PostgreSQL confirma sobre WAL; Docker conserva datos en `banking-data`. No se deshabilita `fsync`. Se requiere un esquema de copias de seguridad para producciÃ³n.

Cliente: apertura y eliminaciÃ³n bloquean primero la fila del cliente. El FK impide que se borre mientras conserve productos. `@Version` aÃ±ade detecciÃ³n de actualizaciones obsoletas en JPA.

Un `requestId` repetido con mismo tipo, importe y cuentas devuelve la operaciÃ³n existente sin afectar saldos. Con datos distintos devuelve 409. En una carrera simultÃ¡nea, la restricciÃ³n Ãºnica garantiza una sola operaciÃ³n; el segundo puede recibir 409 y debe consultar/reintentar con la misma clave. Cambiar la descripciÃ³n se hace mediante PUT, no mediante un reintento de POST.

## Reglas y supuestos explÃ­citos

1. Moneda Ãºnica COP. Fechas de negocio en `America/Bogota`; timestamps almacenados con zona horaria.
2. Cliente adulto desde su cumpleaÃ±os 18. Se valida tambiÃ©n en escrituras SQL directas.
3. IdentificaciÃ³n Ãºnica por tipo y nÃºmero. Nombres/apellidos se recortan y necesitan 2 caracteres efectivos. Email con dominio y extensiÃ³n.
4. Cuentas nuevas activas, saldo cero; prefijos 53 y 33, secuencia global no cÃ­clica de ocho dÃ­gitos. Las secuencias pueden dejar huecos y no se reutilizan.
5. Cuenta corriente admite saldo negativo porque el enunciado limita explÃ­citamente solo el ahorro. No hay lÃ­mite de sobregiro configurable. Es un supuesto de la prueba, no una polÃ­tica de crÃ©dito lista para producciÃ³n.
6. Disponible = saldo: el enunciado no define retenciones, bloqueos de fondos ni cupos. Ambos se actualizan conjuntamente y la base comprueba su igualdad.
7. GMF se registra como indicador; no se calcula impuesto, tarifa ni exclusividad de exenciÃ³n porque no se especificaron.
8. Solo se opera sobre cuentas activas. No se permiten transferencias a la misma cuenta.
9. DELETE de producto cancela lÃ³gicamente, exige cero y conserva historial. Una cancelada no se reactiva. Cliente con cualquier producto, incluso cancelado, no se elimina.
10. CRUD de transacciones: PUT edita descripciÃ³n; DELETE anula una vez y genera movimientos inversos. Importe, tipo y cuentas originales se conservan. Para corregirlos, anular y crear. Una reversiÃ³n que dejarÃ­a ahorros negativos falla completamente.
11. Demo local para evaluaciÃ³n: sin autenticaciÃ³n, autorizaciÃ³n por operador, paginaciÃ³n, auditorÃ­a de identidad o despliegue pÃºblico. La app y Compose se exponen en loopback. Estos controles deben aÃ±adirse antes de uso real.

## Nube

La demostración utiliza un servicio Docker gratuito en Render y PostgreSQL en Neon. Las credenciales se configuran como variables de entorno y no se incluyen en GitHub. GitHub Actions ejecuta JUnit, integración PostgreSQL, pruebas de componentes, cobertura y construcción Docker. La configuración de infraestructura está en `render.yaml`.

## Referencias oficiales

- [Spring Boot: requisitos](https://docs.spring.io/spring-boot/3.5/system-requirements.html)
- [PostgreSQL: transacciones](https://www.postgresql.org/docs/16/tutorial-transactions.html)
- [PostgreSQL: bloqueos explÃ­citos](https://www.postgresql.org/docs/16/explicit-locking.html)
- [Docker Desktop para Windows](https://docs.docker.com/desktop/setup/install/windows-install/)

