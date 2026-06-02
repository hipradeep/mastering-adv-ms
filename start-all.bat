@echo off
title Mastering Advanced Microservices - Starter
echo ========================================================
echo Starting all microservices concurrently...
echo ========================================================

echo [1/7] Starting Eureka Server (Registry)...
start "Eureka Server [Port: 8761]" cmd /k "cd eureka-server && mvn spring-boot:run"

echo Waiting 10 seconds for Eureka Server to initialize...
timeout /t 10 /nobreak > nul

echo [2/7] Starting API Gateway...
start "API Gateway [Port: 8080]" cmd /k "cd api-gateway && mvn spring-boot:run"

echo [3/7] Starting Auth Server...
start "Auth Server [Port: 8081]" cmd /k "cd auth-server && mvn spring-boot:run"

echo [4/7] Starting User Service...
start "User Service [Port: 8082]" cmd /k "cd user-service && mvn spring-boot:run"

echo [5/7] Starting Order Service...
start "Order Service [Port: 8083]" cmd /k "cd order-service && mvn spring-boot:run"

echo [6/7] Starting Inventory Service...
start "Inventory Service [Port: 8084]" cmd /k "cd inventory-service && mvn spring-boot:run"

echo [7/7] Starting Notification Service...
start "Notification Service [Port: 8085]" cmd /k "cd notification-service && mvn spring-boot:run"

echo ========================================================
echo All services have been launched in separate terminals.
echo ========================================================
pause
