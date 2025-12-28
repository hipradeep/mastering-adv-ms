@echo off
setlocal

echo ====================================================
echo Starting Mastering Advanced Microservices Environment
echo ====================================================

REM ----------------------------------------------------
REM 1. Open Command Prompt/Terminal
REM 2. Navigate to project folder
REM 3. Run: run_all.bat
REM ----------------------------------------------------


REM Move to script directory (important for Windows)
cd /d "%~dp0"

REM ----------------------------------------------------
REM 1. Start Zipkin
REM ----------------------------------------------------
echo.
echo [1/5] Starting Zipkin Tracing...

if not exist "zipkin-server.jar" (
    echo ERROR: zipkin-server.jar not found in %cd%
    pause
    exit /b
)

start "Zipkin" cmd /k java -jar zipkin-server.jar

echo Waiting 20 seconds for Zipkin to start...
timeout /t 20 >nul

REM ----------------------------------------------------
REM 2. Start Eureka Server
REM ----------------------------------------------------
echo.
echo [2/5] Starting Eureka Discovery Server...
start "Eureka Server" cmd /k mvn -f eureka-server/pom.xml spring-boot:run

echo Waiting 25 seconds for Eureka to initialize...
timeout /t 25 >nul

REM ----------------------------------------------------
REM 3. Start API Gateway
REM ----------------------------------------------------
echo.
echo [3/5] Starting API Gateway...
start "API Gateway" cmd /k mvn -f gateway-service/pom.xml spring-boot:run

echo Waiting 15 seconds for Gateway to initialize...
timeout /t 15 >nul

REM ----------------------------------------------------
REM 4. Start Core Services
REM ----------------------------------------------------
echo.
echo [4/5] Starting Core Microservices...

start "User Service" cmd /k mvn -f user-service/pom.xml spring-boot:run
start "Inventory Service" cmd /k mvn -f inventory-service/pom.xml spring-boot:run
start "Notification Service" cmd /k mvn -f notification-service/pom.xml spring-boot:run

echo Waiting 15 seconds for services to register...
timeout /t 15 >nul

REM ----------------------------------------------------
REM 5. Start Order Service
REM ----------------------------------------------------
echo.
echo [5/5] Starting Order Service...
start "Order Service" cmd /k mvn -f order-service/pom.xml spring-boot:run

echo ====================================================
echo All services launched successfully on Windows
echo Check opened CMD windows for logs
echo ====================================================
pause
