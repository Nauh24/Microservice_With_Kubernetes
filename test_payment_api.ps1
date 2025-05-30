# Test script for multiple contract payment API

Write-Host "=== Testing Multiple Contract Payment API ===" -ForegroundColor Cyan

# API endpoints
$PAYMENT_SERVICE_URL = "http://localhost:8084"
$CONTRACTS_ENDPOINT = "$PAYMENT_SERVICE_URL/api/customer-payment/customer/5/active-contracts"
$PAYMENT_ENDPOINT = "$PAYMENT_SERVICE_URL/api/customer-payment/multiple-contracts"

$headers = @{
    'Content-Type' = 'application/json'
    'Accept' = 'application/json'
}

try {
    Write-Host "1. Getting active contracts for customer 5..." -ForegroundColor Yellow

    $contracts = Invoke-RestMethod -Uri $CONTRACTS_ENDPOINT -Method GET -Headers $headers
    Write-Host "   Found $($contracts.Count) contracts" -ForegroundColor Green

    # Filter unpaid contracts
    $unpaidContracts = @()
    foreach ($contract in $contracts) {
        $remaining = $contract.totalAmount - $contract.totalPaid
        Write-Host "   Contract ID: $($contract.id) - Total: $($contract.totalAmount) - Paid: $($contract.totalPaid) - Remaining: $remaining" -ForegroundColor White

        if ($remaining -gt 0) {
            $unpaidContracts += $contract
        }
    }

    Write-Host "   Unpaid contracts: $($unpaidContracts.Count)" -ForegroundColor Green

    if ($unpaidContracts.Count -ge 2) {
        Write-Host "2. Testing multiple contract payment..." -ForegroundColor Yellow

        $contract1 = $unpaidContracts[0]
        $contract2 = $unpaidContracts[1]

        $amount1 = [Math]::Min(500000, $contract1.totalAmount - $contract1.totalPaid)
        $amount2 = [Math]::Min(500000, $contract2.totalAmount - $contract2.totalPaid)

        $paymentRequest = @{
            customerId = 5
            totalAmount = $amount1 + $amount2
            paymentMethod = 1
            note = "Test payment multiple contracts - $(Get-Date -Format 'dd/MM/yyyy HH:mm')"
            contractPayments = @(
                @{
                    contractId = $contract1.id
                    allocatedAmount = $amount1
                },
                @{
                    contractId = $contract2.id
                    allocatedAmount = $amount2
                }
            )
        }

        $jsonBody = $paymentRequest | ConvertTo-Json -Depth 3
        Write-Host "   Request Body:" -ForegroundColor White
        Write-Host $jsonBody -ForegroundColor Gray

        $response = Invoke-RestMethod -Uri $PAYMENT_ENDPOINT -Method POST -Body $jsonBody -Headers $headers
        Write-Host "   SUCCESS: Payment created!" -ForegroundColor Green
        Write-Host "   Payment ID: $($response.id)" -ForegroundColor Green
        Write-Host "   Payment Amount: $($response.paymentAmount)" -ForegroundColor Green
        Write-Host "   Contract Payments Count: $($response.contractPayments.Count)" -ForegroundColor Green

    } elseif ($unpaidContracts.Count -eq 1) {
        Write-Host "2. Only one unpaid contract found, testing single payment..." -ForegroundColor Yellow

        $contract = $unpaidContracts[0]
        $amount = [Math]::Min(500000, $contract.totalAmount - $contract.totalPaid)

        $paymentRequest = @{
            customerId = 5
            totalAmount = $amount
            paymentMethod = 1
            note = "Test single payment - $(Get-Date -Format 'dd/MM/yyyy HH:mm')"
            contractPayments = @(
                @{
                    contractId = $contract.id
                    allocatedAmount = $amount
                }
            )
        }

        $jsonBody = $paymentRequest | ConvertTo-Json -Depth 3
        Write-Host "   Request Body:" -ForegroundColor White
        Write-Host $jsonBody -ForegroundColor Gray

        $response = Invoke-RestMethod -Uri $PAYMENT_ENDPOINT -Method POST -Body $jsonBody -Headers $headers
        Write-Host "   SUCCESS: Payment created!" -ForegroundColor Green
        Write-Host "   Payment ID: $($response.id)" -ForegroundColor Green
        Write-Host "   Payment Amount: $($response.paymentAmount)" -ForegroundColor Green

    } else {
        Write-Host "2. No unpaid contracts found for testing" -ForegroundColor Red
    }

} catch {
    Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "Status Code: $statusCode" -ForegroundColor Red

        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Body: $errorBody" -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response" -ForegroundColor Red
        }
    }
}

Write-Host "=== Test Complete ===" -ForegroundColor Cyan
