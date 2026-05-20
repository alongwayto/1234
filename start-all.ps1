$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$logDir = Join-Path $root ".logs"
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

powershell -NoProfile -ExecutionPolicy Bypass -File (Join-Path $root "start-mysql.ps1")

$backendOut = Join-Path $logDir "backend.out.log"
$backendErr = Join-Path $logDir "backend.err.log"
$backendScript = Join-Path $root "start-backend.ps1"
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$backendScript`"" -WorkingDirectory $root -WindowStyle Hidden -RedirectStandardOutput $backendOut -RedirectStandardError $backendErr | Out-Null

$frontendOut = Join-Path $logDir "frontend.out.log"
$frontendErr = Join-Path $logDir "frontend.err.log"
$frontendScript = Join-Path $root "start-frontend.ps1"
Start-Process -FilePath "powershell.exe" -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$frontendScript`"" -WorkingDirectory $root -WindowStyle Hidden -RedirectStandardOutput $frontendOut -RedirectStandardError $frontendErr | Out-Null

Write-Host "Starting services..."
Write-Host "Frontend: http://127.0.0.1:5173/"
Write-Host "Backend:  http://127.0.0.1:8080/api"
Write-Host "Swagger:  http://127.0.0.1:8080/api/swagger-ui.html"
