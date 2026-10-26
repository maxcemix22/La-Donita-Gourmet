@echo off
chcp 65001 >nul
title La Donita Gourmet - Servidor
cd /d "%~dp0JavaNetbeans"

echo ==========================================================
echo    La Donita Gourmet - Sistema Web de Gestion de Pedidos
echo ==========================================================
echo.

rem --- 1. Verificar Java ---
where java >nul 2>nul
if errorlevel 1 (
    echo [ERROR] No se encontro Java. Instala JDK 17 o superior.
    pause
    exit /b 1
)

rem --- 2. Localizar Maven ---
set "MVN=mvn"
where mvn >nul 2>nul
if errorlevel 1 (
    set "MVN="
    for /d %%D in ("C:\Program Files\NetBeans*") do (
        if exist "%%D\netbeans\java\maven\bin\mvn.cmd" set "MVN=%%D\netbeans\java\maven\bin\mvn.cmd"
    )
)

if not defined MVN (
    echo [ERROR] No se encontro Maven.
    pause
    exit /b 1
)

rem --- 3. Seleccionar base de datos ---
echo Elige la base de datos:
echo   1) MySQL
echo   2) Modo DEMO (H2, sin instalar MySQL)
echo.
set "OPCION="
set /p "OPCION=Opcion [2]: "

if "%OPCION%"=="1" (
    set "LADONITA_DB_MODO=mysql"
    set "LADONITA_DB_USER=root"
    set "LADONITA_DB_PASSWORD="
    set /p "LADONITA_DB_USER=Usuario MySQL [root]: "
    set /p "LADONITA_DB_PASSWORD=Contrasena MySQL: "
) else (
    set "LADONITA_DB_MODO=demo"
    set "LADONITA_DB_USER="
    set "LADONITA_DB_PASSWORD="
)

echo.
echo Modo seleccionado: %LADONITA_DB_MODO%
echo.

rem --- 4. Iniciar servidor ---
echo Iniciando La Donita Gourmet...
echo URL: http://localhost:8080/ladonita
echo.

rem Abrir navegador cuando Jetty responda
start "" /b powershell -NoProfile -WindowStyle Hidden -Command "for($i=0;$i -lt 180;$i++){try{Invoke-WebRequest -UseBasicParsing http://localhost:8080/ladonita/login -TimeoutSec 2 | Out-Null; Start-Process 'http://localhost:8080/ladonita'; break}catch{Start-Sleep 2}}"

rem Pasar el modo explícitamente como propiedad del sistema
call "%MVN%" -e org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.14:run

echo.
echo Codigo de salida: %ERRORLEVEL%
pause