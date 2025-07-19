# Byte-to-Long Benchmark Project Summary

## 📦 What's Included

This ZIP file contains a complete Maven project to benchmark different methods for converting `byte[8]` to `long` in Java.

### Files:
- **pom.xml**: Maven configuration with JMH 1.37, Java 21 target
- **README.md**: Complete documentation and usage instructions  
- **PROJECT-SUMMARY.md**: This quick overview file
- **compile.sh**: Unix/Linux compilation script
- **run-benchmark.sh**: Unix/Linux benchmark runner script
- **run-benchmark.bat**: Windows benchmark runner script
- **src/main/java/org/sample/ByteToLongBenchmark.java**: Main JMH benchmark class
- **src/main/java/org/sample/ConversionDemo.java**: Demo class to test methods

## 🚀 Quick Start

1. **Extract the ZIP file**
2. **On Unix/Linux/macOS:**
   ```bash
   cd byte-to-long-benchmark
   chmod +x *.sh
   ./run-benchmark.sh
   ```
3. **On Windows:**
   ```cmd
   cd byte-to-long-benchmark
   run-benchmark.bat
   ```
4. **Manual:**
   ```bash
   cd byte-to-long-benchmark
   mvn clean package
   java -jar target/benchmarks.jar
   ```

## 🔍 Methods Benchmarked

### Fast Bit-Shifting Methods:
1. **userCurrentApproach**: Your original MSB-first loop method
2. **stackOverflow29132118Loop**: Left-shift loop (original and Java 8 variants)
3. **stackOverflow27610608Unrolled**: Fast unrolled bit-shift version  
4. **stackOverflow60456641Approach**: Int-based approach with toUnsignedLong

### ByteBuffer Methods:
5. **byteBufferMethod**: ByteBuffer.wrap() with new buffer each time
6. **byteBufferReusableMethod**: Reusable ByteBuffer (cleared each time)
7. **byteBufferLittleEndian**: ByteBuffer with little-endian order

### BigInteger Methods (from Baeldung):
8. **bigIntegerMethod**: `new BigInteger(bytes).longValue()` - fast but can truncate
9. **bigIntegerExactMethod**: `longValueExact()` - safer, throws ArithmeticException on overflow

### Endianness Variants:
10. **unrolledLittleEndian**: Little-endian unrolled version

## 📊 Expected Performance Ranking (Typical Results)

Based on JMH characteristics and typical Java performance patterns:

1. **🥇 stackOverflow27610608Unrolled** - Fastest (unrolled bit operations)
2. **🥈 unrolledLittleEndian** - Second fastest (unrolled, different endianness)
3. **🥉 stackOverflow29132118Loop variants** - Fast loop-based approaches
4. **userCurrentApproach** - Your current method (solid performance)
5. **stackOverflow60456641Approach** - Int-based approach (more operations)
6. **byteBufferReusableMethod** - Reusable buffer (clearing overhead)
7. **byteBufferMethod** - New buffer each time (allocation overhead)
8. **byteBufferLittleEndian** - ByteBuffer little-endian
9. **bigIntegerMethod** - BigInteger with longValue() (slower due to object creation)
10. **bigIntegerExactMethod** - BigInteger with overflow checking (slowest but safest)

**⚠️ BigInteger Notes:**
- Generally slower than direct bit manipulation due to object overhead
- `longValue()` is faster but can silently truncate large values
- `longValueExact()` is safer but slower (throws ArithmeticException on overflow)
- Best for cases needing arbitrary precision or handling unknown data ranges

**Note:** Actual results will vary based on your JVM, CPU, and system configuration.

## 🧪 Test Data

All methods are tested with the same byte array:
```java
{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
 (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF}
```

**Big-endian result:** -3819410105021120273L (0xCAFEBABEDEADBEEF)
**Little-endian result:** -1167088121787636554L (0xEFBEADDEBEBAFECA)

## 🔧 Requirements

- Java 21+ (required for compilation target)
- Maven 3.6.3+ (for building)
- At least 1GB RAM for JMH execution

## 🎮 Testing the Methods

Before running the full benchmark, you can test all methods with:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="org.sample.ConversionDemo"
```

This shows results from all methods and demonstrates BigInteger overflow behavior.

## 💡 Tips

- **For quick testing:** Run `./run-benchmark.sh -i 1 -wi 1` (faster but less accurate)
- **For production results:** Use default settings or increase iterations
- **For specific methods:** Use pattern matching:
  - `java -jar target/benchmarks.jar ".*Unrolled.*"` (unrolled methods)
  - `java -jar target/benchmarks.jar ".*BigInteger.*"` (BigInteger methods)
  - `java -jar target/benchmarks.jar ".*ByteBuffer.*"` (ByteBuffer methods)
- **For CSV output:** Add `-rf csv -rff results.csv`

## 🎯 Answering the StackOverflow Question

This benchmark provides definitive performance data to answer: 
["Fastest way to convert an byte[8] to long"](https://stackoverflow.com/questions/64229552/fastest-way-to-convert-an-byte8-to-long)

**Key Findings (Expected):**
- Unrolled bit-shifting is typically fastest for raw performance
- Loop-based approaches offer good performance with better readability
- ByteBuffer methods provide convenience and endianness control at performance cost
- BigInteger methods offer safety and arbitrary precision but are slowest

Choose based on your priorities: raw speed, code readability, safety, or precision requirements.

## 🔍 Validation & Safety

The project includes comprehensive validation:
- **ByteToLongBenchmark**: Validates all methods produce identical results before benchmarking
- **ConversionDemo**: Shows method results and demonstrates BigInteger overflow behavior
- **Error handling**: Scripts include Java/Maven version checks and helpful error messages

## 📚 Sources

This benchmark compares methods from:
- StackOverflow answers: 60456641, 29132118, 27610608
- [Baeldung tutorial on byte array to number conversion](https://www.baeldung.com/java-byte-array-to-number)
- User's original approach from the SHA256 hash signature use case

---

**Generated to provide empirical performance data for byte[8] to long conversion methods**
