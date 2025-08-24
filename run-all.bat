@echo off
echo Starting Microservices...

:: Config Server
start cmd /k "cd config-server && mvn spring-boot:run"
timeout /t 10 >nul

:: User Service
start cmd /k "cd user-service && mvn spring-boot:run"
timeout /t 10 >nul

:: Gateway Service
start cmd /k "cd gateway-service && mvn spring-boot:run"
