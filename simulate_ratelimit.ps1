for ($i=1; $i -le 10; $i++) {
    Write-Host "Request $i"
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8082/api/users/1" -Method Get -ErrorAction Stop
        Write-Host "Success: $($response.StatusCode)"
    } catch {
        Write-Host "Failed: $($_.Exception.Message)"
        if ($_.Exception.Response) {
             Write-Host "Status: $($_.Exception.Response.StatusCode)"
        }
    }
    Start-Sleep -Milliseconds 200
}
