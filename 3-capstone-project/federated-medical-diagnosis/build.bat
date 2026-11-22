@echo off
echo ===============================================================================
echo  Building Java Federated Medical AI System...
echo ===============================================================================

if not exist "bin" mkdir "bin"

echo Compiling Java source files...
javac -d bin src\com\medical\federated\config\*.java src\com\medical\federated\model\*.java src\com\medical\federated\service\*.java src\com\medical\federated\server\*.java src\com\medical\federated\gui\*.java src\com\medical\federated\*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Build completed successfully into bin/ directory!
) else (
    echo [ERROR] Build failed! Please ensure JDK 17+ or JDK 21 is installed.
)
pause
