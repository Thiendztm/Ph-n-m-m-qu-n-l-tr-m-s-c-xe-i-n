# Test register với thông tin xe
$registerData = @{
    email = "testuser@gmail.com"
    password = "TestPass123"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "0912345678"
    vehiclePlate = "50X1-14638"
    vehicleModel = "VinFast VF8"
    connectorType = "Type 2"
} | ConvertTo-Json

Write-Host "Registering new user with vehicle info..." -ForegroundColor Green
Write-Host "Data: $registerData" -ForegroundColor Yellow

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" -Method POST -Headers @{"Content-Type"="application/json"} -Body $registerData
    Write-Host "Registration successful!" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 10) -ForegroundColor Cyan
} catch {
    Write-Host "Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody" -ForegroundColor Red
    }
}