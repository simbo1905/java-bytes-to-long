#!/bin/bash

# Script to compile and run the byte-to-long benchmark
# Author: Generated for StackOverflow byte[8] to long benchmark
# Usage: ./run-benchmark.sh [JMH_OPTIONS...]

set -e  # Exit on any error

echo "🚀 Byte-to-Long Conversion Benchmark Runner"
echo "==========================================="

# First, compile the project
echo "🔨 Compiling project..."
./compile.sh

# Check if compilation was successful
if [ ! -f "target/benchmarks.jar" ]; then
    echo "❌ Error: Benchmark JAR not found after compilation"
    exit 1
fi

echo ""
echo "🏃 Starting JMH Benchmark..."
echo "⏱️  This may take several minutes..."
echo ""

# Run the benchmark with any additional arguments passed to this script
if [ $# -eq 0 ]; then
    # Default run - all benchmarks
    echo "📊 Running all benchmark methods..."
    echo "💡 Tip: You can pass JMH options like: $0 -i 3 -wi 3 -f 1"
    echo ""
    java -jar target/benchmarks.jar
else
    # Run with user-provided arguments
    echo "📊 Running with custom options: $@"
    echo ""
    java -jar target/benchmarks.jar "$@"
fi

echo ""
echo "✅ Benchmark completed!"
echo ""
echo "📋 Common JMH options:"
echo "   -i N     : Number of measurement iterations (default: 5)"
echo "   -wi N    : Number of warmup iterations (default: 5)"
echo "   -f N     : Number of forks (default: 1)"
echo "   -t N     : Number of threads (default: 1)"
echo "   -rf json : Output format (json, csv, text)"
echo "   -rff FILE: Output to file"
echo ""
echo "📊 Example runs:"
echo "   $0 -i 3 -wi 3                    # Quick run"
echo "   $0 ".*userCurrentApproach.*"      # Only user's method"
echo "   $0 ".*ByteBuffer.*"               # Only ByteBuffer methods"
echo "   $0 -rf json -rff results.json    # Save results to JSON"
