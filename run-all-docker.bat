@echo off
echo ===== Stopping any running containers =====
docker-compose down

echo ===== Starting all services with Docker Compose =====
docker-compose up -d

echo ===== All services are now running! =====
echo API Gateway is available at http://localhost:8083
echo Frontend is available at http://localhost:3000
