@echo off
echo ===== STEP 1: Updating Java version in pom.xml files =====
call update-java-version.bat

echo ===== STEP 2: Updating Dockerfiles to use Java 17 =====
call update-dockerfiles.bat

echo ===== STEP 3: Building all services =====
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

echo ===== STEP 4: Building frontend =====
cd microservice_fe
call npm install
call npm run build
cd ..

echo ===== STEP 5: Preparing Docker environment =====
REM Đảm bảo script init-multiple-databases.sh có quyền thực thi
powershell -Command "(Get-Item .\init-multiple-databases.sh).IsReadOnly = $false"
powershell -Command "Set-Content -Path .\init-multiple-databases.sh -Value (Get-Content -Path .\init-multiple-databases.sh) -Encoding ASCII"

echo ===== STEP 6: Stopping any running containers =====
docker-compose down

echo ===== STEP 7: Starting all services with Docker Compose =====
docker-compose up -d

echo ===== All services are now running! =====
echo API Gateway is available at http://localhost:8083
echo Frontend is available at http://localhost:3000
