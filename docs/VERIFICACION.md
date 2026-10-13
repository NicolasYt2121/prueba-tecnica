# Verificación de arquitectura hexagonal

13 de octubre de 2026. `mvn verify`: BUILD SUCCESS.

- 52 pruebas de servicios, REST, acceso y arquitectura: 0 fallos, 0 errores.
- 8 pruebas de integración en PostgreSQL 16.15, base aislada nexo_test: 0 fallos, 0 errores, 0 omitidas.
- 8 pruebas de componentes frontend: todas aprobadas tras la migración.
- El núcleo se compiló con javac sin dependencias de Spring/JPA/HTTP.
- Se verificó que los controladores dependan de los puertos de entrada.
- JaCoCo: se cumple el mínimo de 80% de líneas por paquete en Service y Controller.
- No se modificaron migraciones ni datos de la demostración; integración usa una base separada.
- Compilación realizada en una copia temporal de backend porque Windows mantiene bloqueado el JAR de la versión local en ejecución. La copia contiene los mismos fuentes.

Los reportes actuales están en docs/evidencia. La verificación histórica que sigue corresponde a la entrega inicial, antes de la limpieza de datos y el despliegue cloud.

---
# Verificación de entrega

Fecha: 13 de octubre de 2026, America/Bogota.

## Resultados ejecutados

| Verificación | Resultado |
|---|---|
| Compilación y empaquetado Java 21 | `mvn verify`: BUILD SUCCESS |
| JUnit / Mockito / MockMvc | 42 pruebas; 0 fallos; 0 errores |
| Integración con PostgreSQL 16.15 real | 8 pruebas; 0 fallos; 0 errores; 0 omitidas |
| Componentes frontend | 8 pruebas Node; todas aprobadas |
| JaCoCo, líneas Service | 93 cubiertas / 0 omitidas: 100% |
| JaCoCo, ramas Service | 70 cubiertas / 9 omitidas: 88,61% |
| JaCoCo, líneas Controller | 28 cubiertas / 0 omitidas: 100% |
| API de salud, app iniciada | `{"status":"UP"}` |
| Docker Compose | `config --quiet` válido, incluyendo perfil test |
| Contenedor Docker | No ejecutado: motor Docker y WSL no instalados |

La cobertura indicada corresponde a líneas y al código de esta entrega; no equivale a ausencia de defectos. La cobertura de ramas se informa separadamente. Evidencias de JUnit y CSV JaCoCo en `docs/evidencia/`; informe HTML completo generado en `backend/target/site/jacoco/index.html`.

## Casos de integración

1. Restricciones SQL reales: mayoría de edad, FK de cliente, saldo de ahorro y números únicos.
2. Transferencia y reversión conservan dinero y generan débito/crédito.
3. Fondos insuficientes revierten transacción y todos los movimientos.
4. Dos retiros simultáneos de 80 sobre ahorro con 100: solo uno se confirma; saldo final 20.
5. Dos solicitudes simultáneas con el mismo UUID: un único efecto contable; reintento posterior no duplica.
6. Reversión sin fondos en la cuenta receptora: no acredita parcialmente a la cuenta remitente.
7. Cuenta inactiva rechaza operaciones; cancelación exige cero; cliente con cuenta cancelada tampoco se elimina.
8. Cuenta corriente admite sobregiro conforme al supuesto documentado.

## Comprobación en navegador

Se ejecutó el frontend contra el JAR y PostgreSQL, sin respuestas simuladas:

- Rechazo de cliente ficticio nacido en 2015, con mensaje de mayoría de edad.
- Registro del mismo formulario con fecha adulta, listado actualizado.
- Apertura de ahorro `5300000003`, activa y con cero.
- Consignación ficticia de 25,50; saldo y disponible 25,50.
- Estado de cuenta con el crédito y saldo posterior correctos.
- Navegación por clientes, productos, movimientos y vista general.

La base de demostración contiene datos ficticios identificados como Demo/Prueba. El script `scripts/demo.mjs` se ejecutó una vez y se añadió un cliente, una cuenta y una operación desde la interfaz. No son datos bancarios reales. Captura: `docs/panel.png`.

## Herramientas preparadas

Temurin JDK 21.0.12.1, Maven 3.9.11, PostgreSQL 16.15 y Docker Compose standalone 5.6.0 en `work/tools` del espacio de trabajo. JDK, Maven y Compose se verificaron con checksums del proveedor; PostgreSQL se descargó del sitio oficial de EDB mediante HTTPS. El ejecutable Compose permite validar la configuración; no instala un motor de contenedores.

En el entorno restringido de ejecución, Java no podía resolver los directorios antecesores de Documentos. La compilación se realizó usando una unidad temporal `Q:` dirigida exactamente al mismo espacio de trabajo. No se modificaron ACL ni protección de Windows. No se pudieron crear las carpetas temporales propuestas fuera del espacio de trabajo y no se utilizaron.

## Pendientes y límites

- Ejecutar `docker compose up --build -d` y el perfil de pruebas en un equipo con motor Docker/WSL. La validación de Compose no sustituye esta comprobación.
- La preparación local conserva tres commits iniciales. El repositorio remoto tiene un historial independiente creado mediante commits desde la interfaz web de GitHub; consultar docs/GIT-FLOW.md antes de sincronizar.
- Publicación: repositorio privado NicolasYt2121/prueba-tecnica creado y archivos publicados desde el navegador. No se realizó push con GitHub CLI. Los resultados locales de esta página no acreditan por sí solos una ejecución de GitHub Actions.
- No se desplegaron servicios de nube. No se recibió el PDF original: se trabajó con el enunciado enviado en el chat.



