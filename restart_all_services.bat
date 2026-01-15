@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ===================================================
echo Stopping all Terminator Backend Services...
echo ===================================================

echo Checking Port 8083 (Orchestrator)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8083" ^| find "LISTENING"') do (
    echo Killing process %%a...
    taskkill /F /PID %%a >nul 2>&1
)

echo Checking Port 8082 (Inventory)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8082" ^| find "LISTENING"') do (
    echo Killing process %%a...
    taskkill /F /PID %%a >nul 2>&1
)

echo Checking Port 8081 (Issue-to-Patient)...
for /f "tokens=5" %%a in ('netstat -aon ^| find ":8081" ^| find "LISTENING"') do (
    echo Killing process %%a...
    taskkill /F /PID %%a >nul 2>&1
)

echo.
echo ===================================================
echo All services stopped. Waiting 3 seconds...
echo ===================================================
timeout /t 3 /nobreak >nul

echo.
echo Starting Orchestrator Service (Port 8083)...
start "Orchestrator Service" java -jar "D:\Projects Workspaces\terminator\orchestrator-service\target\orchestrator-service-0.0.1-SNAPSHOT.jar"

timeout /t 5 /nobreak >nul

echo Starting Inventory Service (Port 8082)...
start "Inventory Service" java -jar "D:\Projects Workspaces\terminator\inventory-service\target\inventory-service-0.0.1-SNAPSHOT.jar"

timeout /t 5 /nobreak >nul

echo Starting Issue To Patient Service (Port 8081)...
start "Issue To Patient Service" java -jar "D:\Projects Workspaces\terminator\issue-to-patient-service\target\issue-to-patient-service-0.0.1-SNAPSHOT.jar"

echo.
echo ===================================================
echo All services have been launched in separate windows.
echo ===================================================
pause
