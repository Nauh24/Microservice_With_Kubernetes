@echo off
echo Updating Dockerfiles to use Java 17...

cd worker-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd job-schedule-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd register-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd worker-contract-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd customer-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd job-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd customer-contract-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd api-gateway
powershell -Command "(Get-Content Dockerfile) -replace 'FROM eclipse-temurin:24-jdk', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

cd customer-payment-service
powershell -Command "(Get-Content Dockerfile) -replace 'FROM openjdk:17-jdk-slim', 'FROM eclipse-temurin:17-jdk' | Set-Content Dockerfile"
cd ..

echo Dockerfiles updated to use Java 17!
