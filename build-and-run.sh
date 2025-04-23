#!/bin/bash

echo "Building all services..."

cd worker-service
./mvnw clean package -DskipTests
cd ..

cd job-schedule-service
./mvnw clean package -DskipTests
cd ..

cd register-service
./mvnw clean package -DskipTests
cd ..

cd worker-contract-service
./mvnw clean package -DskipTests
cd ..

cd customer-service
./mvnw clean package -DskipTests
cd ..

cd job-service
./mvnw clean package -DskipTests
cd ..

cd customer-contract-service
./mvnw clean package -DskipTests
cd ..

cd api-gateway
./mvnw clean package -DskipTests
cd ..

echo "All services built successfully!"
echo "Starting Docker Compose..."

docker-compose up -d

echo "All services are running!"
echo "API Gateway is available at http://localhost:8083"
