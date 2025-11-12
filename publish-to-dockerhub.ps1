# Script para publicar la imagen de Docker a Docker Hub
# Uso: .\publish-to-dockerhub.ps1 -DockerHubUsername "tu-usuario" -Version "v1.0.0"

param(
    [Parameter(Mandatory=$true)]
    [string]$DockerHubUsername,
    
    [Parameter(Mandatory=$false)]
    [string]$Version = "latest"
)

Write-Host "`n=== Publicando Haversack API a Docker Hub ===" -ForegroundColor Cyan
Write-Host "Usuario: $DockerHubUsername" -ForegroundColor Yellow
Write-Host "Versión: $Version`n" -ForegroundColor Yellow

# Verificar que Docker esté corriendo
docker info >$null 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker no está en ejecución. Inicia Docker Desktop primero." -ForegroundColor Red
    exit 1
}

# 1. Construir la imagen
Write-Host "[1/4] Construyendo imagen..." -ForegroundColor Green
docker-compose build app

if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[ERROR] Falló la construcción de la imagen" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] Imagen construida correctamente`n" -ForegroundColor Green

# 2. Etiquetar la imagen
Write-Host "[2/4] Etiquetando imagen..." -ForegroundColor Green

# Tag con versión específica
docker tag tpo-back_grupo8-app "$DockerHubUsername/haversack-api:$Version"
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[ERROR] Falló el etiquetado de la imagen" -ForegroundColor Red
    exit 1
}

# Tag como latest (solo si no es una versión de dev)
if ($Version -ne "dev") {
    docker tag tpo-back_grupo8-app "$DockerHubUsername/haversack-api:latest"
}

Write-Host "[OK] Imagen etiquetada como:" -ForegroundColor Green
Write-Host "  - $DockerHubUsername/haversack-api:$Version" -ForegroundColor Cyan
if ($Version -ne "dev") {
    Write-Host "  - $DockerHubUsername/haversack-api:latest`n" -ForegroundColor Cyan
} else {
    Write-Host ""
}

# 3. Verificar login
Write-Host "[3/4] Verificando login en Docker Hub..." -ForegroundColor Green
Write-Host "Ingresa tus credenciales de Docker Hub:`n" -ForegroundColor Yellow

docker login
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[ERROR] Login fallido" -ForegroundColor Red
    exit 1
}
Write-Host "`n[OK] Login exitoso`n" -ForegroundColor Green

# 4. Subir la imagen
Write-Host "[4/4] Subiendo imagen a Docker Hub..." -ForegroundColor Green
Write-Host "Esto puede tardar varios minutos dependiendo de tu conexión...`n" -ForegroundColor Yellow

docker push "$DockerHubUsername/haversack-api:$Version"
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n[ERROR] Falló la subida de la imagen" -ForegroundColor Red
    exit 1
}

# Subir latest si corresponde
if ($Version -ne "dev") {
    docker push "$DockerHubUsername/haversack-api:latest"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "`n[ERROR] Falló la subida de la imagen latest" -ForegroundColor Red
        exit 1
    }
}

# Exito
Write-Host "`n======================================" -ForegroundColor Green
Write-Host "   Imagen publicada correctamente!" -ForegroundColor Green
Write-Host "======================================`n" -ForegroundColor Green

Write-Host "Imagen disponible en:" -ForegroundColor Cyan
Write-Host "  https://hub.docker.com/r/$DockerHubUsername/haversack-api`n" -ForegroundColor White

Write-Host "Ahora otros pueden usarla con:" -ForegroundColor Yellow
Write-Host "  docker pull $DockerHubUsername/haversack-api:$Version" -ForegroundColor White
Write-Host "  docker run -p 8080:8080 $DockerHubUsername/haversack-api:$Version`n" -ForegroundColor White

# Mostrar info de la imagen
Write-Host "Informacion de la imagen:" -ForegroundColor Cyan
docker images $DockerHubUsername/haversack-api
Write-Host ""
