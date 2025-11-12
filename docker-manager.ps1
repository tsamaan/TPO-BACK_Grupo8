# Script de utilidad para Docker - Haversack API
# Ejecuta este script en PowerShell para gestionar los contenedores fácilmente

param(
    [Parameter(Mandatory=$false)]
    [ValidateSet('start', 'stop', 'restart', 'logs', 'clean', 'rebuild', 'status', 'db-connect', 'help')]
    [string]$Action = 'help'
)

function Show-Help {
    Write-Host "`n=== Haversack Docker Manager ===" -ForegroundColor Cyan
    Write-Host "`nUso: .\docker-manager.ps1 <accion>`n"
    Write-Host "Acciones disponibles:" -ForegroundColor Yellow
    Write-Host "  start       - Inicia los contenedores (Docker Compose up)"
    Write-Host "  stop        - Detiene los contenedores"
    Write-Host "  restart     - Reinicia los contenedores"
    Write-Host "  logs        - Muestra los logs en tiempo real"
    Write-Host "  clean       - Detiene y elimina todo (incluye volúmenes)"
    Write-Host "  rebuild     - Reconstruye e inicia los contenedores"
    Write-Host "  status      - Muestra el estado de los contenedores"
    Write-Host "  db-connect  - Conecta a la base de datos MySQL"
    Write-Host "  help        - Muestra esta ayuda`n"
}

function Start-Containers {
    Write-Host "`n[INFO] Iniciando contenedores..." -ForegroundColor Green
    docker-compose up -d --build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[SUCCESS] Contenedores iniciados correctamente!" -ForegroundColor Green
        Write-Host "API disponible en: http://localhost:8080/api" -ForegroundColor Cyan
        Write-Host "MySQL disponible en: localhost:3306" -ForegroundColor Cyan
    } else {
        Write-Host "`n[ERROR] Error al iniciar los contenedores" -ForegroundColor Red
    }
}

function Stop-Containers {
    Write-Host "`n[INFO] Deteniendo contenedores..." -ForegroundColor Yellow
    docker-compose down
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[SUCCESS] Contenedores detenidos correctamente!" -ForegroundColor Green
    }
}

function Restart-Containers {
    Write-Host "`n[INFO] Reiniciando contenedores..." -ForegroundColor Yellow
    docker-compose restart
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[SUCCESS] Contenedores reiniciados correctamente!" -ForegroundColor Green
    }
}

function Show-Logs {
    Write-Host "`n[INFO] Mostrando logs (Ctrl+C para salir)..." -ForegroundColor Cyan
    docker-compose logs -f
}

function Clean-All {
    Write-Host "`n[WARNING] Esto eliminará todos los contenedores, redes y volúmenes (incluidos los datos de la BD)" -ForegroundColor Red
    $confirmation = Read-Host "¿Estás seguro? (S/N)"
    if ($confirmation -eq 'S' -or $confirmation -eq 's') {
        Write-Host "`n[INFO] Limpiando todo..." -ForegroundColor Yellow
        docker-compose down -v
        Write-Host "`n[SUCCESS] Limpieza completada!" -ForegroundColor Green
    } else {
        Write-Host "`n[INFO] Operación cancelada" -ForegroundColor Yellow
    }
}

function Rebuild-Containers {
    Write-Host "`n[INFO] Reconstruyendo contenedores..." -ForegroundColor Yellow
    docker-compose down
    docker-compose build --no-cache
    docker-compose up -d
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n[SUCCESS] Contenedores reconstruidos e iniciados!" -ForegroundColor Green
    }
}

function Show-Status {
    Write-Host "`n=== Estado de los Contenedores ===" -ForegroundColor Cyan
    docker-compose ps
}

function Connect-Database {
    Write-Host "`n[INFO] Conectando a MySQL..." -ForegroundColor Cyan
    Write-Host "Usuario: haversack | Password: haversackpass" -ForegroundColor Yellow
    docker exec -it haversack-db mysql -u haversack -phaversackpass haversack_db
}

# Ejecutar acción
switch ($Action) {
    'start'      { Start-Containers }
    'stop'       { Stop-Containers }
    'restart'    { Restart-Containers }
    'logs'       { Show-Logs }
    'clean'      { Clean-All }
    'rebuild'    { Rebuild-Containers }
    'status'     { Show-Status }
    'db-connect' { Connect-Database }
    'help'       { Show-Help }
}
