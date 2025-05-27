# PowerShell script to setup PostgreSQL databases for microservices
# This script creates databases and clears existing data

Write-Host "=== DATABASE SETUP FOR MICROSERVICES ===" -ForegroundColor Green
Write-Host ""

# Function to check if PostgreSQL is running
function Test-PostgreSQLConnection {
    Write-Host "Checking PostgreSQL connection..." -ForegroundColor Yellow

    try {
        $connection = Test-NetConnection -ComputerName localhost -Port 5432 -WarningAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "PostgreSQL is running on localhost:5432" -ForegroundColor Green
            return $true
        } else {
            Write-Host "PostgreSQL is not running on localhost:5432" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "Error checking PostgreSQL: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Function to execute SQL command
function Invoke-SqlCommand {
    param(
        [string]$Command,
        [string]$Database = "postgres"
    )

    try {
        $env:PGPASSWORD = "1234"
        $result = psql -U postgres -d $Database -c $Command 2>&1
        if ($LASTEXITCODE -eq 0) {
            return $true
        } else {
            Write-Host "SQL Error: $result" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "Error executing SQL: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# Check PostgreSQL connection
if (-not (Test-PostgreSQLConnection)) {
    Write-Host "Please start PostgreSQL service and try again." -ForegroundColor Yellow
    exit 1
}

Write-Host "1. CREATING DATABASES..." -ForegroundColor Cyan

# Create databases
$databases = @("customerdb", "jobdb", "customercontractdb", "customerpaymentdb")

foreach ($db in $databases) {
    Write-Host "Creating database: $db" -ForegroundColor Yellow
    $createResult = Invoke-SqlCommand -Command "CREATE DATABASE $db;" -Database "postgres"
    if ($createResult) {
        Write-Host "✓ Database $db created successfully" -ForegroundColor Green
    } else {
        Write-Host "ℹ Database $db may already exist" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "2. CLEARING EXISTING DATA..." -ForegroundColor Cyan

# Clear job service data
Write-Host "Clearing job service data..." -ForegroundColor Yellow
$jobClearCommands = @(
    "DROP TABLE IF EXISTS work_shifts CASCADE;",
    "DROP TABLE IF EXISTS job_details CASCADE;",
    "DROP TABLE IF EXISTS jobs CASCADE;",
    "DELETE FROM job_categories WHERE id > 0;"
)

foreach ($cmd in $jobClearCommands) {
    Invoke-SqlCommand -Command $cmd -Database "jobdb" | Out-Null
}

# Clear contract service data
Write-Host "Clearing contract service data..." -ForegroundColor Yellow
$contractClearCommands = @(
    "DROP TABLE IF EXISTS work_shifts CASCADE;",
    "DROP TABLE IF EXISTS job_details CASCADE;",
    "DROP TABLE IF EXISTS customer_contracts CASCADE;"
)

foreach ($cmd in $contractClearCommands) {
    Invoke-SqlCommand -Command $cmd -Database "customercontractdb" | Out-Null
}

# Clear payment service data
Write-Host "Clearing payment service data..." -ForegroundColor Yellow
Invoke-SqlCommand -Command "DELETE FROM customer_payments WHERE id > 0;" -Database "customerpaymentdb" | Out-Null

Write-Host ""
Write-Host "3. SETTING UP VIETNAMESE JOB CATEGORIES..." -ForegroundColor Cyan

# Setup Vietnamese job categories
$jobCategories = @(
    "('Công nhân xây dựng', 'Công việc liên quan đến xây dựng, thi công các công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ điện', 'Lắp đặt, sửa chữa hệ thống điện trong các công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ nước', 'Lắp đặt, sửa chữa hệ thống cấp thoát nước', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ hàn', 'Hàn các kết cấu thép, kim loại trong xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ sơn', 'Sơn tường, sơn kết cấu các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Công nhân vận chuyển', 'Vận chuyển vật liệu, hàng hóa tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ mộc', 'Làm đồ gỗ, ván khuôn, cốp pha cho xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ ốp lát', 'Ốp lát gạch, đá cho các công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Công nhân dọn dẹp', 'Dọn dẹp vệ sinh công trình, khu vực làm việc', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ cơ khí', 'Sửa chữa, bảo trì máy móc thiết bị xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Công nhân bảo vệ', 'Bảo vệ an ninh tại các công trình xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ làm vườn', 'Chăm sóc cây xanh, thiết kế cảnh quan', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Công nhân kho bãi', 'Quản lý, sắp xếp vật tư trong kho', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ lái xe', 'Lái xe tải, xe máy xúc tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Công nhân phụ việc', 'Hỗ trợ các công việc phụ tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Nhân viên văn phòng', 'Công việc hành chính, văn thư tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Kỹ thuật viên', 'Hỗ trợ kỹ thuật, giám sát chất lượng công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ máy', 'Vận hành máy móc thiết bị xây dựng', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Công nhân an toàn', 'Đảm bảo an toàn lao động tại công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    "('Thợ cắt gạch', 'Cắt, gia công gạch đá cho công trình', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)"
)

$insertSql = "INSERT INTO job_categories (name, description, is_deleted, created_at, updated_at) VALUES " + ($jobCategories -join ", ") + " ON CONFLICT (name) DO NOTHING;"

Write-Host "Inserting Vietnamese job categories..." -ForegroundColor Yellow
$insertResult = Invoke-SqlCommand -Command $insertSql -Database "jobdb"

if ($insertResult) {
    Write-Host "✓ Vietnamese job categories inserted successfully" -ForegroundColor Green
} else {
    Write-Host "✗ Failed to insert job categories" -ForegroundColor Red
}

Write-Host ""
Write-Host "🎉 DATABASE SETUP COMPLETED!" -ForegroundColor Green
Write-Host ""
Write-Host "DATABASES CREATED:" -ForegroundColor Cyan
Write-Host "  ✓ customerdb" -ForegroundColor White
Write-Host "  ✓ jobdb (with Vietnamese job categories)" -ForegroundColor White
Write-Host "  ✓ customercontractdb" -ForegroundColor White
Write-Host "  ✓ customerpaymentdb" -ForegroundColor White
Write-Host ""
Write-Host "Next step: Run .\build-and-deploy.ps1 to build and deploy microservices" -ForegroundColor Yellow
