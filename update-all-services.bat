@echo off
echo Updating all services...

cd api-gateway
call mvn clean install -DskipTests
cd ..

cd customer-contract-service
call mvn clean install -DskipTests
cd ..

cd customer-service
call mvn clean install -DskipTests
cd ..

cd job-schedule-service
call mvn clean install -DskipTests
cd ..

cd job-service
call mvn clean install -DskipTests
cd ..

cd register-service
call mvn clean install -DskipTests
cd ..

cd worker-contract-service
call mvn clean install -DskipTests
cd ..

cd worker-service
call mvn clean install -DskipTests
cd ..

echo All services updated!
