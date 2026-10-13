# Versionamiento

Repositorio privado: https://github.com/NicolasYt2121/prueba-tecnica

Un único repositorio contiene backend, frontend, infraestructura, documentación y pruebas. La entrega se publicó mediante commits de la interfaz web de GitHub, organizados por componentes. No se realizó un push desde el equipo: GitHub CLI no está autenticado en el entorno del agente. Los tres commits de preparación local tienen un historial independiente del publicado.

La rama `main` contiene la entrega. Para continuar el trabajo se recomienda crear `develop` desde main y utilizar `feature/<nombre>` desde develop, con pull requests revisados. Para futuras entregas, usar `release/<version>` y reintegrar a main y develop; para correcciones urgentes, `hotfix/<nombre>` desde main. Este flujo recomendado no se presenta como historial ya ejecutado.

Para sincronizar trabajo futuro, clonar el repositorio publicado en una carpeta nueva y trabajar desde ese historial. No forzar un push del historial local inicial sobre el remoto.

```bash
git clone https://github.com/NicolasYt2121/prueba-tecnica.git
cd prueba-tecnica
git switch -c develop
git push -u origin develop
```

Se necesita una cuenta invitada al repositorio y autenticación de Git. No subir `.env`, datos de PostgreSQL, ejecutables descargados ni caches. Antes de cada commit revisar `git status` y `git diff --cached`. Los scripts de publicación inicial se conservan como referencia; no son necesarios para clonar o ejecutar la entrega.
