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
echo Map successful. Kafka will run from W:\

echo ==================================================
echo Step 1: Building Projects...
echo ==================================================
echo Building common-dtos...
cd common-dtos
call mvn clean install
cd ..

echo Building Services...
start /b cmd /c "cd order-service && mvn clean install -DskipTests"
start /b cmd /c "cd payment-service && mvn clean install -DskipTests"
start /b cmd /c "cd inventory-service && mvn clean install -DskipTests"

echo Waiting for builds to complete (approx 30s)...
timeout /t 30

echo ==================================================
echo Step 2: Starting Infrastructure (Kafka)...
echo ==================================================
echo Starting Zookeeper (from W:\)...
start "Zookeeper" cmd /k "W: && cd bin\windows && zookeeper-server-start.bat ..\..\config\zookeeper.properties"
timeout /t 10

echo Starting Kafka Broker (from W:\)...
start "Kafka Broker" cmd /k "W: && cd bin\windows && kafka-server-start.bat ..\..\config\server.properties"
timeout /t 15

echo ==================================================
echo Step 3: Starting Microservices...
echo ==================================================
echo Starting Order Service...
start "Order Service" cmd /k "java -jar order-service/target/order-service-0.0.1-SNAPSHOT.jar"

echo Starting Payment Service...
start "Payment Service" cmd /k "java -jar payment-service/target/payment-service-0.0.1-SNAPSHOT.jar"

echo Starting Inventory Service...
start "Inventory Service" cmd /k "java -jar inventory-service/target/inventory-service-0.0.1-SNAPSHOT.jar"


echo ==================================================
echo All services started!
echo NOTE: Drive W: is now mapped to your kafka folder.
echo You can remove it later with 'subst W: /d' if needed.
echo ==================================================
echo Use POST http://localhost:8081/orders to test.
echo ==================================================
pause
