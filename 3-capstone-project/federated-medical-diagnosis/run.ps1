# PowerShell Runner for Java Federated Medical AI System
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host " Launching Java-Based Privacy-Preserving Federated Medical AI Platform..." -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan

Set-Location -Path $PSScriptRoot

if (-not (Test-Path -Path "bin\com\medical\federated\Main.class")) {
    Write-Host "[INFO] Compiling Java classes..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path "bin" | Out-Null
    javac -d bin (Get-ChildItem -Path "src" -Filter *.java -Recurse).FullName
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
        Exit
    }
}

Write-Host "[SUCCESS] Running Application (GUI + HTTP Server on port 8080)..." -ForegroundColor Green
java -cp bin com.medical.federated.Main
