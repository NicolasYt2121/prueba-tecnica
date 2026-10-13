# Arquitectura hexagonal

## Dependencias

```text
HTTP -> controller + dto -> application.port.in <- service -> application.port.out
                                  |                  |                 ^
                                  +---- domain ------+                 |
                                                         repository/Jpa*Adapter -> entity/JPA -> PostgreSQL
config: construcción de servicios e inyección de puertos + decoradores transaccionales
```

- `domain.model`: clientes, cuentas, transacciones, movimientos y enumeraciones Java sin anotaciones de persistencia.
- `application.Commands`: entradas de los casos de uso independientes del transporte.
- `application.port.in`: contratos CustomerUseCases, AccountUseCases y TransactionUseCases.
- `service`: implementación de casos de uso y reglas de negocio; solo depende de Java y del núcleo.
- `application.port.out`: contratos de persistencia y generación del número de cuenta.
- `controller` y `dto`: adaptador de entrada REST, validación HTTP y conversión a comandos. Las rutas y JSON conservan su formato.
- `repository`: adaptadores de salida JPA, mapeo explícito, consultas y secuencia PostgreSQL.
- `entity`: modelos exclusivos de persistencia; nunca se entregan al núcleo.
- `config`: raíz de composición y decoradores Spring de transacciones.

## Patrones y SOLID

Puertos y adaptadores, Repository, inyección por constructor, DTO, Mapper y Decorator. La inversión de dependencias se expresa en interfaces del núcleo, implementadas por infraestructura. Los controladores dependen de casos de uso, no de servicios concretos ni de JPA. Los servicios pueden probarse sin arrancar Spring. Cada adaptador traduce un límite concreto y puede sustituirse sin cambiar reglas del negocio.

## ACID y concurrencia

Los decoradores envuelven cada escritura completa en una transacción. Una transferencia incluye registro, débito, crédito y movimientos; una excepción revierte todo. Los puertos de bloqueo se implementan con PESSIMISTIC_WRITE dentro de esa transacción, en orden ascendente de cuenta. El identificador generado se devuelve antes de guardar movimientos. Se mantienen @Version, restricciones SQL e idempotencia por UUID. El estado de cuenta usa REPEATABLE_READ. No se modifica el esquema ni se borran datos existentes.

## Verificación

`mvn verify` ejecuta pruebas de servicios, REST y reglas arquitectónicas. Con TEST_DB_URL apuntando exclusivamente a una base dedicada terminada en `test`, ejecuta además integración real con PostgreSQL: rollback, transferencias, anulaciones, concurrencia e idempotencia. El test arquitectónico compila el núcleo con Java sin dependencias externas, y comprueba que los controladores consuman puertos de entrada.
