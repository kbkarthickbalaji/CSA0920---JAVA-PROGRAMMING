@echo off
echo ===============================================================================
echo  Launching Java-Based Privacy-Preserving Federated Medical AI Platform...
echo ===============================================================================

cd /d "%~dp0"
if not exist "bin\com\medical\federated\Main.class" (
    echo [INFO] Binaries not found. Triggering automated build...
    call build.bat
)

echo Starting Java Platform & Web Server on http://localhost:8080 ...
java -cp bin com.medical.federated.Main
pause
