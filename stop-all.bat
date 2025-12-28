@echo off
echo ====================================================
echo Stopping Mastering Advanced Microservices Environment
echo ====================================================

echo Killing service windows...

taskkill /FI "WINDOWTITLE eq Zipkin*" /T /F >nul 2>&1
echo Stopped Zipkin

taskkill /FI "WINDOWTITLE eq Eureka Server*" /T /F >nul 2>&1
echo Stopped Eureka Server

taskkill /FI "WINDOWTITLE eq API Gateway*" /T /F >nul 2>&1
echo Stopped API Gateway

taskkill /FI "WINDOWTITLE eq User Service*" /T /F >nul 2>&1
echo Stopped User Service

taskkill /FI "WINDOWTITLE eq Inventory Service*" /T /F >nul 2>&1
echo Stopped Inventory Service

taskkill /FI "WINDOWTITLE eq Notification Service*" /T /F >nul 2>&1
echo Stopped Notification Service

taskkill /FI "WINDOWTITLE eq Order Service*" /T /F >nul 2>&1
echo Stopped Order Service

echo ====================================================
echo All services stopped.
echo ====================================================
pause
