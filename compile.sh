#!/bin/bash

# Script to compile the byte-to-long benchmark project
# Author: Generated for StackOverflow byte[8] to long benchmark
# Usage: ./compile.sh

set -e  # Exit on any error

echo "🔨 Compiling Byte-to-Long Conversion Benchmark..."
echo "=================================================="

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed or not in PATH"
    echo "Please install Java 21 or higher"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Error: Java 21 or higher is required"
    echo "Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Error: Maven is not installed or not in PATH"
    echo "Please install Apache Maven 3.6.3 or higher"
    exit 1
fi

# Clean and compile
echo "🧹 Cleaning previous builds..."
mvn clean -q

echo "📦 Compiling and packaging..."
mvn package -q -DskipTests

# Check if the JAR was created successfully
if [ -f "target/benchmarks.jar" ]; then
    echo "✅ Compilation successful!"
    echo ""
    echo "📊 Benchmark JAR created: target/benchmarks.jar"
    echo "📏 JAR size: $(du -h target/benchmarks.jar | cut -f1)"
    echo ""
    echo "🚀 To run the benchmark:"
    echo "   ./run-benchmark.sh"
    echo "   or"
    echo "   java -jar target/benchmarks.jar"
    echo ""
    echo "💡 For specific benchmarks:"
    echo "   java -jar target/benchmarks.jar ".*userCurrentApproach.*""
    echo "   java -jar target/benchmarks.jar ".*ByteBuffer.*""
else
    echo "❌ Error: Failed to create benchmarks.jar"
    echo "Check the Maven output above for errors"
    exit 1
fi
