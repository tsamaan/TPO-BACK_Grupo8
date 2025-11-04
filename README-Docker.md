Docker instructions for Haversack

Build and run with Docker Compose (app + MySQL):

1. Build and start services:

   ```powershell
   docker compose up --build -d
   ```

2. Check logs:

   ```powershell
   docker compose logs -f app
   ```

3. Stop and remove:

   ```powershell
   docker compose down -v
   ```

Important notes:
- `docker-compose.yml` expone MySQL en 3306 y la app en 8080.
- La app usa variables de entorno para configurar la conexión a la DB. Revisa `SPRING_DATASOURCE_*` si necesitas cambiarlas.
- El Dockerfile usa Maven Wrapper (`mvnw`). Asegúrate que está en el repo (ya existe en este proyecto).

If you prefer to run only the app using a locally running MySQL, set the datasource env vars accordingly in your shell or compose override.
