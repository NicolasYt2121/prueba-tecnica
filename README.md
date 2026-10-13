# Prueba Tecnica — Nexo Finanzas

Aplicación de administración de clientes, cuentas y movimientos para una entidad financiera. **Java 21 + Spring Boot 3.5.16 + PostgreSQL 16**, arquitectura MVC por capas y frontend por componentes HTML/CSS/JavaScript.

## Abrir en este equipo

Ejecuta `Iniciar.ps1` en PowerShell desde esta carpeta. Usa las herramientas portátiles instaladas en la carpeta `work` del espacio de trabajo y la base local ya preparada. Luego abre **http://localhost:8080**. `Detener.ps1` detiene la app y PostgreSQL, conservando datos. No mover la carpeta `work` si se utiliza este modo.

```powershell
.\Iniciar.ps1
```

No requiere instalar un IDE. Java, Maven y PostgreSQL se descargaron desde sus proveedores oficiales; no se modificó el PATH global. Los binarios y la contraseña local no forman parte del repositorio.

Para completar los commits y ramas finales desde tu PowerShell: `.\Finalizar-Git.ps1`. La escritura final de `.git` está bloqueada para el entorno del agente. Con un repositorio de GitHub creado y Git autenticado, se puede publicar con `.\Finalizar-Git.ps1 -RemoteUrl https://github.com/USUARIO/prueba-tecnica.git`.

Cuenta indicada: **NicolasYt2121**. En este equipo, ejecutar `.\Publicar-GitHub.ps1` inicia la autenticación oficial de GitHub, finaliza los commits y crea el repositorio privado `NicolasYt2121/prueba-tecnica`. Comprueba la cuenta activa, evita sobrescribir un repositorio con historial y verifica el commit remoto después del push. La publicación no se ha ejecutado todavía.

## Ejecutar con Docker en otro equipo

Requisitos: Docker Engine / Docker Desktop con Compose v2. En Windows, Docker Desktop necesita virtualización y WSL 2 según los [requisitos oficiales](https://docs.docker.com/desktop/setup/install/windows-install/).

```bash
cp .env.example .env
# Editar DB_PASSWORD en .env antes de arrancar.
docker compose up --build -d
```

En PowerShell, sustituir `cp` por `Copy-Item`. Abrir http://localhost:8080. Backend y frontend se sirven juntos; PostgreSQL permanece en la red interna. `docker compose down` conserva el volumen. No usar `down -v` si se quieren conservar datos.

En este equipo no se encontró Docker ni WSL. **La construcción y ejecución del contenedor quedan pendientes de verificación en un entorno con Docker**. El backend sí se verifica directamente con Java y PostgreSQL reales; consultar `docs/VERIFICACION.md` para los resultados finales.

## Compilar y ejecutar sin Docker

Instalar JDK 21, Maven 3.9+ y PostgreSQL 16. Crear una base `nexo` y un usuario con permisos para ejecutar la migración Flyway. Configurar:

```powershell
$env:DB_URL='jdbc:postgresql://localhost:5432/nexo'
$env:DB_USER='nexo'
$env:DB_PASSWORD='TU_CLAVE'
cd backend
.\mvnw.cmd verify
java -jar target/banking-1.0.0.jar
```

En Linux/macOS: `./mvnw verify`; usar `export` para las variables. Si Maven ya está instalado se puede usar `mvn` directamente. El wrapper descarga Maven 3.9.11. Flyway crea el esquema en el primer arranque; Hibernate lo valida.

## Pruebas y cobertura

```bash
cd backend
./mvnw verify
```

En Windows usar `mvnw.cmd`. Ejecuta los tests unitarios y de controladores. Para incluir los **tests de integración PostgreSQL**, crear una base independiente cuyo nombre termine en `test` (por ejemplo `nexo_test`) y establecer `TEST_DB_URL`, `DB_USER`, `DB_PASSWORD` antes de `verify`. Esas pruebas limpian exclusivamente la base de prueba configurada; no usar la base de demostración.

```powershell
$env:TEST_DB_URL='jdbc:postgresql://localhost:5432/nexo_test'
$env:DB_USER='nexo'
$env:DB_PASSWORD='TU_CLAVE'
.\mvnw.cmd verify
```

Reportes: `backend/target/site/jacoco/index.html`, `target/surefire-reports`, `target/failsafe-reports`. JaCoCo exige al menos **80% de cobertura de líneas por paquete** en Service y Controller. Sin TEST_DB_URL la integración se omite explícitamente, no se considera ejecutada.

Pruebas de componentes de frontend, desde la raíz (Node.js 22+):

```bash
node --test frontend-tests/*.test.mjs
```

Pruebas dockerizadas con base aislada:

```bash
docker compose --profile test run --rm tests
```

El servicio `test-db` es independiente de los datos de la aplicación. GitHub Actions incorpora PostgreSQL, `mvn verify`, componentes y `docker build` en cada push/PR.

## Probar desde la interfaz

1. **Clientes → Registrar cliente:** registrar un adulto; probar un menor para observar el rechazo.
2. **Productos → Abrir cuenta:** seleccionar titular y ahorro/corriente. Verificar prefijo, diez dígitos, estado activo y cero.
3. **Movimientos → Nuevo movimiento:** consignar, retirar y transferir a otra cuenta.
4. **Productos → Estado de cuenta:** consultar débito/crédito, saldo posterior y descargar CSV.
5. **Movimientos → Anular:** comprobar los asientos inversos; no desaparece el registro original.
6. Intentar cancelar con saldo, retirar más del saldo de ahorro o eliminar un cliente con cuentas; se rechazan sin alterar saldos.

Opcional: `node scripts/demo.mjs` carga datos **ficticios** mediante la API. No se insertan automáticamente en el arranque.

## Documentación

- `docs/ARQUITECTURA.md`: capas, patrones, SOLID, ACID y supuestos.
- `docs/API.md`: endpoints REST, contratos y errores.
- `docs/DML.sql`: ejemplos de INSERT, SELECT, UPDATE, DELETE y reconciliación.
- `backend/src/main/resources/db/migration/V1__banking_schema.sql`: DDL, índices, triggers y restricciones.
- `docs/REQUERIMIENTOS.md`: matriz de trazabilidad del texto recibido.
- `docs/GIT-FLOW.md`: ramas, commits, publicación y mantenimiento.
- `docs/VERIFICACION.md`: resultados observados y limitaciones pendientes.

## Alcance de la demostración

Sin autenticación de empleados: ejecutar solo localmente. No es un sistema bancario listo para producción. Las decisiones no especificadas —sobregiro de corriente, disponible igual a saldo, GMF como indicador y eliminación lógica con reversión— están documentadas. El código está publicado en GitHub; la aplicación no se ha desplegado como servicio de nube.

