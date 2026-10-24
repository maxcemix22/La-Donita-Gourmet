@echo off
chcp 65001 >nul
title La Donita Gourmet - Servidor
cd /d "%~dp0JavaNetbeans"

echo ==========================================================
echo    La Donita Gourmet - Sistema Web de Gestion de Pedidos
echo ==========================================================
echo.

rem --- 1. Java ---
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] No se encontro Java. Instala el JDK 17 o superior.
    pause
    exit /b 1
)

rem --- 2. Maven: primero el del PATH, luego el que trae NetBeans ---
set "MVN=mvn"
where mvn >nul 2>nul
if errorlevel 1 (
    set "MVN="
    for /d %%D in ("C:\Program Files\NetBeans*") do (
        if exist "%%D\netbeans\java\maven\bin\mvn.cmd" set "MVN=%%D\netbeans\java\maven\bin\mvn.cmd"
    )
)
if not defined MVN (
    echo [ERROR] No se encontro Maven. Instala NetBeans o Apache Maven.
    pause
    exit /b 1
)

rem --- 3. Base de datos ---
echo Elige la base de datos:
echo   1) MySQL  (antes ejecuta en MySQL los scripts de la carpeta "BaseDatos": 01, 02 y opcional 03)
echo   2) Modo DEMO (base embebida con datos de ejemplo, no necesita instalar MySQL)
echo.
set "OPCION="
set /p "OPCION=Opcion [2]: "
if "%OPCION%"=="1" (
    set "LADONITA_DB_MODO=mysql"
    set "LADONITA_DB_USER="
    set "LADONITA_DB_PASSWORD="
    set /p "LADONITA_DB_USER=Usuario de MySQL [root]: "
    set /p "LADONITA_DB_PASSWORD=Contrasena de MySQL (Enter si no tiene): "
) else (
    set "LADONITA_DB_MODO=demo"
)
if not defined LADONITA_DB_USER set "LADONITA_DB_USER=root"

echo.
echo Iniciando el servidor en modo %LADONITA_DB_MODO%... la primera vez tarda unos minutos (descarga dependencias).
echo El navegador se abrira solo cuando el sistema este listo: http://localhost:8080/ladonita
echo Usuarios de prueba: admin_donita / Admin2026!   cperez_emp / Empleado2026!   jsalas_rep / Reparto2026!
echo Para detenerlo presiona Ctrl+C en esta ventana.
echo.

rem Abre el navegador cuando el servidor responda
start "" /b powershell -NoProfile -WindowStyle Hidden -Command "for($i=0;$i -lt 180;$i++){try{Invoke-WebRequest -UseBasicParsing http://localhost:8080/ladonita/login -TimeoutSec 2 | Out-Null; Start-Process 'http://localhost:8080/ladonita'; break}catch{Start-Sleep 2}}"

call "%MVN%" -q org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.14:run
pause
