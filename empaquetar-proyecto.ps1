# Script para empaquetar el proyecto para compartir
# Crea diferentes paquetes según el caso de uso

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('usuario', 'desarrollador', 'produccion')]
    [string]$Tipo = 'usuario'
)

$fecha = Get-Date -Format "yyyy-MM-dd"
$carpetaDestino = ".\distribucion-$fecha"

Write-Host "`n=== Empaquetando Haversack API ===" -ForegroundColor Cyan
Write-Host "Tipo de paquete: $Tipo`n" -ForegroundColor Yellow

# Crear carpeta de destino
if (Test-Path $carpetaDestino) {
    Remove-Item $carpetaDestino -Recurse -Force
}
New-Item -ItemType Directory -Path $carpetaDestino | Out-Null

switch ($Tipo) {
    'usuario' {
        Write-Host "[Usuario Final] Copiando archivos necesarios para ejecutar..." -ForegroundColor Green
        
        # Archivos esenciales
        Copy-Item "docker-compose.yml" $carpetaDestino
        Copy-Item "Dockerfile" $carpetaDestino
        Copy-Item ".dockerignore" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "start-docker.bat" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "GUIA_USUARIO.md" $carpetaDestino
        
        # Renombrar GUIA_USUARIO.md a README.md
        if (Test-Path "$carpetaDestino\GUIA_USUARIO.md") {
            Rename-Item "$carpetaDestino\GUIA_USUARIO.md" "README.md"
        }
        
        Write-Host "`nArchivos incluidos:" -ForegroundColor Cyan
        Write-Host "  - docker-compose.yml"
        Write-Host "  - Dockerfile"
        Write-Host "  - .dockerignore"
        Write-Host "  - start-docker.bat"
        Write-Host "  - README.md (guía de usuario)"
    }
    
    'desarrollador' {
        Write-Host "[Desarrollador] Copiando proyecto completo..." -ForegroundColor Green
        
        # Copiar todo el código fuente
        Copy-Item "src" $carpetaDestino -Recurse
        Copy-Item "pom.xml" $carpetaDestino
        Copy-Item "Dockerfile" $carpetaDestino
        Copy-Item "docker-compose.yml" $carpetaDestino
        Copy-Item ".dockerignore" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item ".gitignore" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "mvnw" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "mvnw.cmd" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item ".mvn" $carpetaDestino -Recurse -ErrorAction SilentlyContinue
        
        # Documentación
        Copy-Item "README.md" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "DOCKER_README.md" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "DOCKER_CHEATSHEET.md" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "COMPARTIR_PROYECTO.md" $carpetaDestino -ErrorAction SilentlyContinue
        
        # Scripts útiles
        Copy-Item "start-docker.bat" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "docker-manager.ps1" $carpetaDestino -ErrorAction SilentlyContinue
        Copy-Item "publish-to-dockerhub.ps1" $carpetaDestino -ErrorAction SilentlyContinue
        
        # Crear .env.example
        Copy-Item ".env.example" $carpetaDestino -ErrorAction SilentlyContinue
        
        Write-Host "`nArchivos incluidos:" -ForegroundColor Cyan
        Write-Host "  - src/ (código fuente completo)"
        Write-Host "  - pom.xml"
        Write-Host "  - Archivos Docker"
        Write-Host "  - Documentación completa"
        Write-Host "  - Scripts de ayuda"
    }
    
    'produccion' {
        Write-Host "[Producción] Copiando archivos para deployment..." -ForegroundColor Green
        
        # Crear docker-compose.prod.yml
        $dockerComposeProd = @"
services:
  db:
    image: mysql:8.0
    container_name: haversack-db-prod
    restart: always
    environment:
      MYSQL_DATABASE: `${DB_NAME}
      MYSQL_USER: `${DB_USER}
      MYSQL_PASSWORD: `${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: `${DB_ROOT_PASSWORD}
      TZ: UTC
    volumes:
      - db_data:/var/lib/mysql
    networks:
      - haversack-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  app:
    image: felogalli/haversack-api:latest
    container_name: haversack-app-prod
    restart: always
    depends_on:
      db:
        condition: service_healthy
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/`${DB_NAME}?useSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: `${DB_USER}
      SPRING_DATASOURCE_PASSWORD: `${DB_PASSWORD}
      JWT_SECRET: `${JWT_SECRET}
      JWT_EXPIRATION: `${JWT_EXPIRATION:-86400000}
      TZ: UTC
    ports:
      - "`${APP_PORT:-8080}:8080"
    networks:
      - haversack-network

networks:
  haversack-network:
    driver: bridge

volumes:
  db_data:
"@
        
        $dockerComposeProd | Out-File "$carpetaDestino\docker-compose.prod.yml" -Encoding UTF8
        
        # Crear .env.template
        $envTemplate = @"
# Configuración de Base de Datos
DB_NAME=haversack_db
DB_USER=haversack_user
DB_PASSWORD=CAMBIAR_PASSWORD_SEGURO
DB_ROOT_PASSWORD=CAMBIAR_ROOT_PASSWORD_SEGURO

# Configuración JWT
JWT_SECRET=CAMBIAR_POR_CLAVE_SECRETA_LARGA_Y_SEGURA_DE_AL_MENOS_256_BITS
JWT_EXPIRATION=86400000

# Puerto de la aplicación
APP_PORT=8080
"@
        
        $envTemplate | Out-File "$carpetaDestino\.env.template" -Encoding UTF8
        
        # Crear README de deployment
        $deploymentReadme = @"
# Haversack API - Deployment en Producción

## Configuración

1. Copia .env.template a .env
2. Edita .env con valores de producción seguros
3. Ejecuta: docker-compose -f docker-compose.prod.yml up -d

## Variables de entorno importantes

- DB_PASSWORD: Contraseña segura para la base de datos
- JWT_SECRET: Clave secreta para tokens JWT (mínimo 256 bits)
- APP_PORT: Puerto donde correrá la aplicación (default: 8080)

## Comandos útiles

Ver logs: docker-compose -f docker-compose.prod.yml logs -f
Detener: docker-compose -f docker-compose.prod.yml down
Actualizar: docker-compose -f docker-compose.prod.yml pull && docker-compose -f docker-compose.prod.yml up -d

## Seguridad

- Nunca uses las contraseñas de desarrollo en producción
- Mantén el archivo .env seguro y fuera del control de versiones
- Usa HTTPS en producción
"@
        
        $deploymentReadme | Out-File "$carpetaDestino\README.md" -Encoding UTF8
        
        Write-Host "`nArchivos incluidos:" -ForegroundColor Cyan
        Write-Host "  - docker-compose.prod.yml"
        Write-Host "  - .env.template"
        Write-Host "  - README.md (instrucciones deployment)"
    }
}

# Comprimir
$archivoZip = "haversack-api-$Tipo-$fecha.zip"
Write-Host "`nComprimiendo archivos..." -ForegroundColor Yellow

if (Test-Path $archivoZip) {
    Remove-Item $archivoZip -Force
}

Compress-Archive -Path "$carpetaDestino\*" -DestinationPath $archivoZip -Force

# Limpiar carpeta temporal
Remove-Item $carpetaDestino -Recurse -Force

Write-Host "`n======================================" -ForegroundColor Green
Write-Host "  Paquete creado exitosamente!" -ForegroundColor Green
Write-Host "======================================`n" -ForegroundColor Green

Write-Host "Archivo: $archivoZip" -ForegroundColor Cyan
Write-Host "Tamaño: $((Get-Item $archivoZip).Length / 1KB) KB`n" -ForegroundColor Cyan

Write-Host "Próximos pasos:" -ForegroundColor Yellow
Write-Host "  1. Comparte el archivo $archivoZip"
Write-Host "  2. El receptor debe descomprimir el archivo"

if ($Tipo -eq 'usuario') {
    Write-Host "  3. Ejecutar: .\start-docker.bat`n"
} elseif ($Tipo -eq 'desarrollador') {
    Write-Host "  3. Ejecutar: docker-compose up -d --build`n"
} else {
    Write-Host "  3. Configurar .env y ejecutar: docker-compose -f docker-compose.prod.yml up -d`n"
}
