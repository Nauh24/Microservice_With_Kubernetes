@echo off
echo Starting all microservices...

start cmd /k "cd api-gateway && mvn spring-boot:run"
timeout /t 5

start cmd /k "cd customer-service && mvn spring-boot:run"
timeout /t 5

start cmd /k "cd customer-contract-service && mvn spring-boot:run"
timeout /t 5

start cmd /k "cd job-service && mvn spring-boot:run"
timeout /t 5

start cmd /k "cd job-schedule-service && mvn spring-boot:run"
timeout /t 5

start cmd /k "cd register-service && mvn spring-boot:run"
timeout /t 5

start cmd /k "cd worker-service && mvn spring-boot:run"
timeout /t 5

start cmd /k "cd worker-contract-service && mvn spring-boot:run"
timeout /t 5

echo All services started!
echo Press any key to stop all services...
pause > nul

echo Stopping all services...
taskkill /F /FI "WINDOWTITLE eq api-gateway*"
taskkill /F /FI "WINDOWTITLE eq customer-service*"
taskkill /F /FI "WINDOWTITLE eq customer-contract-service*"
taskkill /F /FI "WINDOWTITLE eq job-service*"
taskkill /F /FI "WINDOWTITLE eq job-schedule-service*"
taskkill /F /FI "WINDOWTITLE eq register-service*"
taskkill /F /FI "WINDOWTITLE eq worker-service*"
taskkill /F /FI "WINDOWTITLE eq worker-contract-service*"

echo All services stopped!
