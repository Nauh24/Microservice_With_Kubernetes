# Simple database setup script
Write-Host "=== DATABASE SETUP FOR MICROSERVICES ===" -ForegroundColor Green
Write-Host ""

# Check PostgreSQL connection
Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow
try {
    $connection = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue
    if ($connection.TcpTestSucceeded) {
        Write-Host "PostgreSQL is running on localhost:5432" -ForegroundColor Green
    } else {
        Write-Host "PostgreSQL is not running on localhost:5432" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "Error checking PostgreSQL" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Creating databases..." -ForegroundColor Yellow

# Set PostgreSQL password
$env:PGPASSWORD = "1234"

# Create databases
$databases = @("customerdb", "jobdb", "customercontractdb", "customerpaymentdb")

foreach ($db in $databases) {
    Write-Host "Creating database: $db" -ForegroundColor Yellow
    try {
        psql -U postgres -d postgres -c "CREATE DATABASE $db;" 2>$null
        Write-Host "Database $db created or already exists" -ForegroundColor Green
    } catch {
        Write-Host "Database $db may already exist" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Database setup completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next step: Run build-and-deploy.ps1" -ForegroundColor Yellow
