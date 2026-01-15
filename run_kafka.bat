@echo off
echo Starting Saga Pattern Project...

REM Workaround for "Input line is too long" error in Kafka on Windows
REM Mapping the project Kafka directory to drive W: to shorten the path
set "KAFKA_DIR=%~dp0kafka"

echo ==================================================
echo Step 0: Checking Path Configuration
echo ==================================================
if exist W:\ (
    echo Drive W: is already mapped. Attempting to unmap...
    subst W: /d
    if exist W:\ (
        echo ERROR: Could not unmap Drive W:. It might be in use.
        echo Please allow the script to map the kafka folder to a short drive letter W.
        pause
        exit /b
    )
)

echo Mapping %KAFKA_DIR% to Drive W:...
subst W: "%KAFKA_DIR%"
if not exist W:\ (
    echo ERROR: Failed to map drive W:. Please check permissions.
    pause
    exit /b
)
echo ==================================================
echo Step 1: Starting Infrastructure (Kafka)...
echo ==================================================
echo Starting Zookeeper (from W:\)...
start "Zookeeper" cmd /k "W: && cd bin\windows && zookeeper-server-start.bat ..\..\config\zookeeper.properties"
timeout /t 10

echo Starting Kafka Broker (from W:\)...
start "Kafka Broker" cmd /k "W: && cd bin\windows && kafka-server-start.bat ..\..\config\server.properties"
timeout /t 15
echo Map successful. Kafka will run from W:\

pause
