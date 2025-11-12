@echo off
REM Script de inicio rápido para Haversack API con Docker
REM Ejecuta este archivo .bat para iniciar todo automáticamente

echo.
echo ======================================
echo   Haversack API - Inicio con Docker
echo ======================================
echo.

REM Verificar si Docker está corriendo
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker no esta en ejecucion.
    echo Por favor, inicia Docker Desktop primero.
    pause
    exit /b 1
)

echo [INFO] Docker detectado correctamente
echo.

REM Verificar si el puerto 3306 está en uso
netstat -ano | findstr :3306 >nul
if %errorlevel% equ 0 (
    echo [WARNING] El puerto 3306 esta en uso.
    echo Si tienes XAMPP corriendo, debes detener MySQL primero.
    echo.
    pause
)

echo [INFO] Construyendo e iniciando contenedores...
echo.
docker-compose up -d --build

if %errorlevel% equ 0 (
    echo.
    echo ======================================
    echo   Contenedores iniciados correctamente!
    echo ======================================
    echo.
    echo API disponible en: http://localhost:8080/api
    echo MySQL disponible en: localhost:3306
    echo.
    echo Credenciales de MySQL:
    echo   Usuario: haversack
    echo   Password: haversackpass
    echo   Database: haversack_db
    echo.
    echo Para ver logs: docker-compose logs -f
    echo Para detener: docker-compose down
    echo.
) else (
    echo.
    echo [ERROR] Hubo un error al iniciar los contenedores
    echo.
)

pause
