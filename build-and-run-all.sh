#!/bin/bash

echo "===== STEP 1: Building all services ====="

cd customer-service
./mvnw clean package -DskipTests
cd ..

cd job-service
./mvnw clean package -DskipTests
cd ..

cd customer-contract-service
./mvnw clean package -DskipTests
cd ..

cd customer-payment-service
./mvnw clean package -DskipTests
cd ..

cd customer-statistics-service
./mvnw clean package -DskipTests
cd ..

cd api-gateway
./mvnw clean package -DskipTests
cd ..

echo "===== STEP 2: Building frontend ====="
cd microservice_fe
npm install
npm run build
cd ..

echo "===== STEP 3: Preparing Docker environment ====="
chmod +x init-multiple-databases.sh

echo "===== STEP 4: Stopping any running containers ====="
docker-compose down

echo "===== STEP 5: Starting all services with Docker Compose ====="
docker-compose up -d

echo "===== All services are now running! ====="
echo "API Gateway is available at http://localhost:8083"
echo "Frontend is available at http://localhost:3000"
