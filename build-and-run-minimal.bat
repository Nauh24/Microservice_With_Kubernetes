@echo off
echo ===== STEP 1: Building all services =====
cd customer-service
call mvn clean package -DskipTests
cd ..

cd job-service
call mvn clean package -DskipTests
cd ..

cd customer-contract-service
call mvn clean package -DskipTests
cd ..

cd customer-payment-service
call mvn clean package -DskipTests
cd ..

cd customer-statistics-service
call mvn clean package -DskipTests
cd ..

cd api-gateway
call mvn clean package -DskipTests
cd ..

echo ===== STEP 2: Building frontend =====
cd microservice_fe
call npm install
call npm run build
cd ..

echo ===== STEP 3: Preparing Docker environment =====
REM Đảm bảo script init-multiple-databases.sh có quyền thực thi
powershell -Command "(Get-Item .\init-multiple-databases.sh).IsReadOnly = $false"
powershell -Command "Set-Content -Path .\init-multiple-databases.sh -Value (Get-Content -Path .\init-multiple-databases.sh) -Encoding ASCII"

echo ===== STEP 4: Stopping any running containers =====
docker-compose -f docker-compose-new.yml down

echo ===== STEP 5: Starting all services with Docker Compose =====
docker-compose -f docker-compose-new.yml up -d

echo ===== All services are now running! =====
echo API Gateway is available at http://localhost:8083
echo Frontend is available at http://localhost:3000
