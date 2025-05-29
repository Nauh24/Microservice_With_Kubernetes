#!/usr/bin/env pwsh

# Script to stop all microservices
Write-Host "🛑 Stopping All Microservices..." -ForegroundColor Red

# Function to kill processes on specific ports
function Stop-ServiceOnPort {
    param([int]$Port)
    
    try {
        $processes = netstat -ano | findstr ":$Port " | ForEach-Object {
            $fields = $_ -split '\s+' | Where-Object { $_ -ne '' }
            if ($fields.Length -ge 5) {
                $fields[4]  # PID is usually the 5th field
            }
        } | Sort-Object -Unique
        
        foreach ($pid in $processes) {
            if ($pid -and $pid -match '^\d+$') {
                Write-Host "Stopping process on port $Port (PID: $pid)..." -ForegroundColor Yellow
                try {
                    Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue
                    Write-Host "✅ Stopped process $pid on port $Port" -ForegroundColor Green
                } catch {
                    Write-Host "⚠️ Could not stop process $pid" -ForegroundColor Yellow
                }
            }
        }
    } catch {
        Write-Host "⚠️ No process found on port $Port" -ForegroundColor Yellow
    }
}

# Stop all microservice ports
Write-Host "Stopping services on their respective ports..." -ForegroundColor Cyan

Stop-ServiceOnPort -Port 8080  # API Gateway
Stop-ServiceOnPort -Port 8081  # Customer Service
Stop-ServiceOnPort -Port 8082  # Job Service
Stop-ServiceOnPort -Port 8083  # Customer Contract Service
Stop-ServiceOnPort -Port 8084  # Customer Payment Service
Stop-ServiceOnPort -Port 8085  # Customer Statistics Service

# Also stop any Java processes that might be running Maven
Write-Host "Stopping any remaining Java/Maven processes..." -ForegroundColor Cyan
try {
    Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object {
        $_.ProcessName -eq "java" -and 
        ($_.CommandLine -like "*spring-boot*" -or $_.CommandLine -like "*maven*")
    } | ForEach-Object {
        Write-Host "Stopping Java process: $($_.Id)" -ForegroundColor Yellow
        Stop-Process -Id $_.Id -Force -ErrorAction SilentlyContinue
    }
} catch {
    Write-Host "No additional Java processes to stop" -ForegroundColor Gray
}

Write-Host ""
Write-Host "🎉 All microservices have been stopped!" -ForegroundColor Green
Write-Host ""
Write-Host "To restart all services, run:" -ForegroundColor Cyan
Write-Host "  .\start-all-services.ps1" -ForegroundColor White
