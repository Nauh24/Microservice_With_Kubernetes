# Simple script to build Docker images using docker-compose

Write-Host "Building all Docker images using docker-compose..." -ForegroundColor Green

# Build all images
docker-compose build

if ($LASTEXITCODE -eq 0) {
    Write-Host "All Docker images built successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Available commands:" -ForegroundColor Cyan
    Write-Host "  docker-compose up -d          # Start all services in background" -ForegroundColor White
    Write-Host "  docker-compose up             # Start all services with logs" -ForegroundColor White
    Write-Host "  docker-compose down           # Stop all services" -ForegroundColor White
    Write-Host "  docker-compose logs [service] # View logs" -ForegroundColor White
    Write-Host ""
    Write-Host "Services will be available at:" -ForegroundColor Cyan
    Write-Host "  Frontend:                     http://localhost:3000" -ForegroundColor White
    Write-Host "  API Gateway:                  http://localhost:8080" -ForegroundColor White
    Write-Host "  Customer Service:             http://localhost:8081" -ForegroundColor White
    Write-Host "  Job Service:                  http://localhost:8082" -ForegroundColor White
    Write-Host "  Customer Contract Service:    http://localhost:8083" -ForegroundColor White
    Write-Host "  Customer Payment Service:     http://localhost:8084" -ForegroundColor White
    Write-Host "  Customer Statistics Service:  http://localhost:8085" -ForegroundColor White
    Write-Host "  PostgreSQL Database:          localhost:5432" -ForegroundColor White
} else {
    Write-Host "Failed to build Docker images" -ForegroundColor Red
    Write-Host "Please check the error messages above and fix any issues." -ForegroundColor Yellow
    exit 1
}
