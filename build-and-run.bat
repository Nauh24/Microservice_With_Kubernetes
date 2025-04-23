@echo off
echo Updating Java version and Dockerfiles...

call update-java-version.bat
call update-dockerfiles.bat

echo Building all services...

cd worker-service
call mvn clean package -DskipTests
cd ..

cd job-schedule-service
call mvn clean package -DskipTests
cd ..

cd register-service
call mvn clean package -DskipTests
cd ..

cd worker-contract-service
call mvn clean package -DskipTests
cd ..

cd customer-service
call mvn clean package -DskipTests
cd ..

cd job-service
call mvn clean package -DskipTests
cd ..

cd customer-contract-service
call mvn clean package -DskipTests
cd ..

cd api-gateway
call mvn clean package -DskipTests
cd ..

echo All services built successfully!
echo Starting Docker Compose...

REM Đảm bảo script init-multiple-databases.sh có quyền thực thi
powershell -Command "(Get-Item .\init-multiple-databases.sh).IsReadOnly = $false"
powershell -Command "Set-Content -Path .\init-multiple-databases.sh -Value (Get-Content -Path .\init-multiple-databases.sh) -Encoding ASCII"

docker-compose up -d

echo All services are running!
echo API Gateway is available at http://localhost:8083
