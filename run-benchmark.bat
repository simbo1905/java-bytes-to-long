@echo off
REM Script to compile and run the byte-to-long benchmark on Windows
REM Usage: run-benchmark.bat [JMH_OPTIONS...]

echo 🚀 Byte-to-Long Conversion Benchmark Runner (Windows)
echo ====================================================

REM Check if Java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error: Java is not installed or not in PATH
    echo Please install Java 21 or higher
    pause
    exit /b 1
)

REM Check if Maven is available  
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error: Maven is not installed or not in PATH
    echo Please install Apache Maven 3.6.3 or higher
    pause
    exit /b 1
)

echo 🔨 Compiling project...
call mvn clean package -q -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Error: Compilation failed
    pause
    exit /b 1
)

echo ✅ Compilation successful!
echo.
echo 🏃 Starting JMH Benchmark...
echo ⏱️  This may take several minutes...
echo.

if "%~1"=="" (
    echo 📊 Running all benchmark methods...
    java -jar target\benchmarks.jar
) else (
    echo 📊 Running with custom options: %*
    java -jar target\benchmarks.jar %*
)

echo.
echo ✅ Benchmark completed!
echo.
echo Press any key to exit...
pause >nul
