@echo off
if "%~1"=="esperar" goto esperar
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

rem Vigilante: relanza este mismo .bat minimizado para abrir el navegador cuando el servidor responda
start "" /min cmd /c ""%~f0" esperar"

rem Iniciar Jetty
echo [DEBUG] Carpeta: %CD%
if exist pom.xml (echo [DEBUG] pom.xml OK) else (echo [DEBUG] NO hay pom.xml aqui)

if /i "%MVN%"=="mvn" (
    call mvn.cmd -e org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.14:run
) else (
    call "%MVN%" -e org.eclipse.jetty.ee10:jetty-ee10-maven-plugin:12.0.14:run
)

echo.
echo Codigo de salida: %ERRORLEVEL%
pause
exit /b

:esperar
set /a N=0
:bucle
curl -s -o nul http://127.0.0.1:8080/ladonita/ && goto abrir
set /a N+=1
if %N% geq 300 exit /b
timeout /t 2 /nobreak >nul
goto bucle
:abrir
start "" http://localhost:8080/ladonita
exit /b