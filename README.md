# Prueba Tecnica â€” Nexo Finanzas

AplicaciÃ³n de administraciÃ³n de clientes, cuentas y movimientos para una entidad financiera. **Java 21 + Spring Boot 3.5.16 + PostgreSQL 16**, arquitectura hexagonal (puertos y adaptadores) y frontend por componentes HTML/CSS/JavaScript.

## Abrir en este equipo

Ejecuta `Iniciar.ps1` en PowerShell desde esta carpeta. Usa las herramientas portÃ¡tiles instaladas en la carpeta `work` del espacio de trabajo y la base local ya preparada. Luego abre **http://localhost:8080**. `Detener.ps1` detiene la app y PostgreSQL, conservando datos. No mover la carpeta `work` si se utiliza este modo.

```powershell
.\Iniciar.ps1
```

No requiere instalar un IDE. Java, Maven y PostgreSQL se descargaron desde sus proveedores oficiales; no se modificÃ³ el PATH global. Los binarios y la contraseÃ±a local no forman parte del repositorio.

Repositorio: https://github.com/NicolasYt2121/prueba-tecnica. La entrega se publicÃ³ mediante commits desde la interfaz de GitHub. Para continuar en otro equipo, clonar el repositorio. Los scripts Finalizar-Git.ps1 y Publicar-GitHub.ps1 son herramientas de preparaciÃ³n inicial; no usarlos para sobrescribir el historial remoto.

## Ejecutar con Docker en otro equipo

Requisitos: Docker Engine / Docker Desktop con Compose v2. En Windows, Docker Desktop necesita virtualizaciÃ³n y WSL 2 segÃºn los [requisitos oficiales](https://docs.docker.com/desktop/setup/install/windows-install/).

```bash
cp .env.example .env
# Editar DB_PASSWORD en .env antes de arrancar.
docker compose up --build -d
```

En PowerShell, sustituir `cp` por `Copy-Item`. Abrir http://localhost:8080. Backend y frontend se sirven juntos; PostgreSQL permanece en la red interna. `docker compose down` conserva el volumen. No usar `down -v` si se quieren conservar datos.

En este equipo no se encontrÃ³ Docker ni WSL. **La construcciÃ³n y ejecuciÃ³n del contenedor quedan pendientes de verificaciÃ³n en un entorno con Docker**. El backend sÃ­ se verifica directamente con Java y PostgreSQL reales; consultar `docs/VERIFICACION.md` para los resultados finales.

## Compilar y ejecutar sin Docker

Instalar JDK 21, Maven 3.9+ y PostgreSQL 16. Crear una base `nexo` y un usuario con permisos para ejecutar la migraciÃ³n Flyway. Configurar:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/nexo'
$env:DB_USER='nexo'
$env:DB_PASSWORD='TU_CLAVE'
cd backend
.\mvnw.cmd verify
java -jar target/banking-1.0.0.jar
```

En Linux/macOS: `sh ./mvnw verify`; usar `export` para las variables. Si Maven ya estÃ¡ instalado se puede usar `mvn` directamente. El wrapper descarga Maven 3.9.11. Flyway crea el esquema en el primer arranque; Hibernate lo valida.

## Pruebas y cobertura

```bash
cd backend
sh ./mvnw verify
```

En Windows usar `mvnw.cmd`. Ejecuta los tests unitarios y de controladores. Para incluir los **tests de integraciÃ³n PostgreSQL**, crear una base independiente cuyo nombre termine en `test` (por ejemplo `nexo_test`) y establecer `TEST_DB_URL`, `DB_USER`, `DB_PASSWORD` antes de `verify`. Esas pruebas limpian exclusivamente la base de prueba configurada; no usar la base de demostraciÃ³n.

```powershell
$env:TEST_DB_URL='jdbc:postgresql://localhost:5432/nexo_test'
$env:DB_USER='nexo'
$env:DB_PASSWORD='TU_CLAVE'
.\mvnw.cmd verify
```

Reportes: `backend/target/site/jacoco/index.html`, `target/surefire-reports`, `target/failsafe-reports`. JaCoCo exige al menos **80% de cobertura de lÃ­neas por paquete** en Service y Controller. Sin TEST_DB_URL la integraciÃ³n se omite explÃ­citamente, no se considera ejecutada.

Pruebas de componentes de frontend, desde la raÃ­z (Node.js 22+):

```bash
node --test frontend-tests/*.test.mjs
```

Pruebas dockerizadas con base aislada:

```bash
docker compose --profile test run --rm tests
```

El servicio `test-db` es independiente de los datos de la aplicaciÃ³n. GitHub Actions incorpora PostgreSQL, `mvn verify`, componentes y `docker build` en cada push/PR.

## Probar desde la interfaz

1. **Clientes â†’ Registrar cliente:** registrar un adulto; probar un menor para observar el rechazo.
2. **Productos â†’ Abrir cuenta:** seleccionar titular y ahorro/corriente. Verificar prefijo, diez dÃ­gitos, estado activo y cero.
3. **Movimientos â†’ Nuevo movimiento:** consignar, retirar y transferir a otra cuenta.
4. **Productos â†’ Estado de cuenta:** consultar dÃ©bito/crÃ©dito, saldo posterior y descargar CSV.
5. **Movimientos â†’ Anular:** comprobar los asientos inversos; no desaparece el registro original.
6. Intentar cancelar con saldo, retirar mÃ¡s del saldo de ahorro o eliminar un cliente con cuentas; se rechazan sin alterar saldos.

Opcional: `node scripts/demo.mjs` carga datos **ficticios** mediante la API. No se insertan automÃ¡ticamente en el arranque.

## DocumentaciÃ³n

- `docs/ARQUITECTURA.md`: capas, patrones, SOLID, ACID y supuestos.
- `docs/API.md`: endpoints REST, contratos y errores.
- `docs/DML.sql`: ejemplos de INSERT, SELECT, UPDATE, DELETE y reconciliaciÃ³n.
- `backend/src/main/resources/db/migration/V1__banking_schema.sql`: DDL, Ã­ndices, triggers y restricciones.
- `docs/REQUERIMIENTOS.md`: matriz de trazabilidad del texto recibido.
- `docs/GIT-FLOW.md`: ramas, commits, publicaciÃ³n y mantenimiento.
- `docs/VERIFICACION.md`: resultados observados y limitaciones pendientes.

## Alcance de la demostraciÃ³n

Sin autenticaciÃ³n de empleados: ejecutar solo localmente. No es un sistema bancario listo para producciÃ³n. Las decisiones no especificadas â€”sobregiro de corriente, disponible igual a saldo, GMF como indicador y eliminaciÃ³n lÃ³gica con reversiÃ³nâ€” estÃ¡n documentadas. El cÃ³digo estÃ¡ publicado en GitHub; la aplicaciÃ³n no se ha desplegado como servicio de nube.



