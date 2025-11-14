# Test profile với token mới
$token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckBnbWFpbC5jb20iLCJpYXQiOjE3NjMxMzI4NTEsImV4cCI6MTc2MzIxOTI1MX0.P5W20wXcpXytE_C1Pz-9LSRZ8VkaBhMLku_evHRJi9M"

Write-Host "Getting profile info for testuser@gmail.com..." -ForegroundColor Green

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/profile" -Method GET -Headers @{"Authorization"="Bearer $token"; "Content-Type"="application/json"}
    Write-Host "Profile data retrieved successfully!" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 10) -ForegroundColor Cyan
} catch {
    Write-Host "Profile request failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "Error response: $responseBody" -ForegroundColor Red
    }
}