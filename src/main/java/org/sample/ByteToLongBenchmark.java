package org.sample;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.math.BigInteger;

/**
 * Benchmarks different methods for converting byte[8] to long.
 * 
 * This benchmark compares various approaches from StackOverflow answers:
 * - https://stackoverflow.com/a/60456641/5647659
 * - https://stackoverflow.com/a/29132118/5647659  
 * - https://stackoverflow.com/a/27610608/5647659
 * - https://www.baeldung.com/java-byte-array-to-number (BigInteger approach)
 * 
 * And includes the user's current approach plus additional methods.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class ByteToLongBenchmark {

    // Test data - fixed 8 bytes representing some arbitrary long value
    private static final byte[] TEST_BYTES = new byte[] {
        (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
        (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF
    };

    // Expected result for validation
    private static final long EXPECTED_RESULT = -3819410105021120785L;

    // Reusable ByteBuffer for methods that can benefit from reuse
    private ByteBuffer reusableBuffer;

    @Setup
    public void setup() {
        // Initialize reusable buffer
        reusableBuffer = ByteBuffer.allocate(8);

        // Verify all methods produce the same result
        long result1 = userCurrentApproach(TEST_BYTES, 0);
        long result2 = stackOverflow60456641Approach(TEST_BYTES, 0);
        long result3 = stackOverflow29132118Loop(TEST_BYTES);
        long result4 = stackOverflow29132118LoopJava8(TEST_BYTES);
        long result5 = stackOverflow27610608Unrolled(TEST_BYTES);
        long result6 = byteBufferMethod(TEST_BYTES);
        long result7 = byteBufferReusableMethod(TEST_BYTES);
        long result8 = bigIntegerMethod(TEST_BYTES);

        if (result1 != result2 || result1 != result3 || result1 != result4 || 
            result1 != result5 || result1 != result6 || result1 != result7 ||
            result1 != result8) {
            throw new IllegalStateException("Methods produce different results! " +
                "result1=" + result1 + ", result2=" + result2 + ", result3=" + result3 + 
                ", result4=" + result4 + ", result5=" + result5 + ", result6=" + result6 + 
                ", result7=" + result7 + ", result8=" + result8);
        }

        if( result1 != EXPECTED_RESULT) {
            throw new IllegalStateException("Methods produce incorrect result! " +
                "Expected: " + EXPECTED_RESULT + ", but got: " + result1);
        }
    }

    /**
     * User's current approach - loop with bit shifting from MSB
     */
    @Benchmark
    public void userCurrentApproach(Blackhole bh) {
        long result = userCurrentApproach(TEST_BYTES, 0);
        bh.consume(result);
    }

    private static long userCurrentApproach(byte[] b, int offset) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result |= ((long) b[offset + i] & 0xff) << (56 - (i * 8));
        }
        return result;
    }

    /**
     * StackOverflow answer https://stackoverflow.com/a/60456641/5647659
     * Converts using high/low int approach with toUnsignedLong
     */
    @Benchmark
    public void stackOverflow60456641Approach(Blackhole bh) {
        long result = stackOverflow60456641Approach(TEST_BYTES, 0);
        bh.consume(result);
    }

    private static long stackOverflow60456641Approach(byte[] bytes, int offset) {
        return Integer.toUnsignedLong(bytesToInt(bytes, offset)) << Integer.SIZE |
               Integer.toUnsignedLong(bytesToInt(bytes, offset + Integer.BYTES));
    }

    private static int bytesToInt(byte[] bytes, int offset) {
        return (bytes[offset + Integer.BYTES - 1] & 0xFF) |
               (bytes[offset + Integer.BYTES - 2] & 0xFF) << Byte.SIZE |
               (bytes[offset + Integer.BYTES - 3] & 0xFF) << Byte.SIZE * 2 |
               (bytes[offset + Integer.BYTES - 4] & 0xFF) << Byte.SIZE * 3;
    }

    /**
     * StackOverflow answer https://stackoverflow.com/a/29132118/5647659
     * Loop-based approach with left shift
     */
    @Benchmark
    public void stackOverflow29132118Loop(Blackhole bh) {
        long result = stackOverflow29132118Loop(TEST_BYTES);
        bh.consume(result);
    }

    private static long stackOverflow29132118Loop(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    /**
     * StackOverflow answer https://stackoverflow.com/a/29132118/5647659
     * Java 8+ version using constants
     */
    @Benchmark
    public void stackOverflow29132118LoopJava8(Blackhole bh) {
        long result = stackOverflow29132118LoopJava8(TEST_BYTES);
        bh.consume(result);
    }

    private static long stackOverflow29132118LoopJava8(byte[] b) {
        long result = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    /**
     * StackOverflow answer https://stackoverflow.com/a/27610608/5647659
     * Fast unrolled version
     */
    @Benchmark
    public void stackOverflow27610608Unrolled(Blackhole bh) {
        long result = stackOverflow27610608Unrolled(TEST_BYTES);
        bh.consume(result);
    }

    private static long stackOverflow27610608Unrolled(byte[] b) {
        return ((long) b[0] << 56)
             | ((long) b[1] & 0xff) << 48
             | ((long) b[2] & 0xff) << 40
             | ((long) b[3] & 0xff) << 32
             | ((long) b[4] & 0xff) << 24
             | ((long) b[5] & 0xff) << 16
             | ((long) b[6] & 0xff) << 8
             | ((long) b[7] & 0xff);
    }

    /**
     * ByteBuffer method - creates new buffer each time
     */
    @Benchmark
    public void byteBufferMethod(Blackhole bh) {
        long result = byteBufferMethod(TEST_BYTES);
        bh.consume(result);
    }

    private static long byteBufferMethod(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
    }

    /**
     * ByteBuffer method - reuses buffer (requires clearing state)
     */
    @Benchmark
    public void byteBufferReusableMethod(Blackhole bh) {
        long result = byteBufferReusableMethod(TEST_BYTES);
        bh.consume(result);
    }

    private long byteBufferReusableMethod(byte[] bytes) {
        reusableBuffer.clear();
        reusableBuffer.put(bytes);
        reusableBuffer.flip();
        return reusableBuffer.getLong();
    }

    /**
     * BigInteger method from https://www.baeldung.com/java-byte-array-to-number
     * WARNING: BigInteger.longValue() can lose precision if the value is outside long range
     * For exact conversion, use longValueExact() which throws ArithmeticException on overflow
     */
    @Benchmark
    public void bigIntegerMethod(Blackhole bh) {
        long result = bigIntegerMethod(TEST_BYTES);
        bh.consume(result);
    }

    private static long bigIntegerMethod(byte[] bytes) {
        return new BigInteger(bytes).longValue();
    }

    /**
     * BigInteger exact method - throws ArithmeticException if value doesn't fit in long
     * This is safer but potentially slower due to overflow checking
     */
    @Benchmark
    public void bigIntegerExactMethod(Blackhole bh) {
        long result = bigIntegerExactMethod(TEST_BYTES);
        bh.consume(result);
    }

    private static long bigIntegerExactMethod(byte[] bytes) {
        return new BigInteger(bytes).longValueExact();
    }

    /**
     * Alternative unrolled version with different bit order
     * (Little-endian style for comparison)
     */
    @Benchmark
    public void unrolledLittleEndian(Blackhole bh) {
        long result = unrolledLittleEndian(TEST_BYTES);
        bh.consume(result);
    }

    private static long unrolledLittleEndian(byte[] b) {
        return ((long) b[0] & 0xff)
             | ((long) b[1] & 0xff) << 8
             | ((long) b[2] & 0xff) << 16
             | ((long) b[3] & 0xff) << 24
             | ((long) b[4] & 0xff) << 32
             | ((long) b[5] & 0xff) << 40
             | ((long) b[6] & 0xff) << 48
             | ((long) b[7] & 0xff) << 56;
    }

    /**
     * ByteBuffer Little Endian method
     */
    @Benchmark
    public void byteBufferLittleEndian(Blackhole bh) {
        long result = byteBufferLittleEndian(TEST_BYTES);
        bh.consume(result);
    }

    private static long byteBufferLittleEndian(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    // Main method to run benchmarks
    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
