package org.sample;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Simple demo class to show how the different byte-to-long conversion methods work.
 * This can be used to verify the methods produce correct results.
 */
public class ConversionDemo {

  // Test data
  private static final byte[] TEST_BYTES = new byte[]{
      (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE,
      (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF
  };

  public static void main(String[] args) {
    System.out.println("Byte-to-Long Conversion Methods Demo");
    System.out.println("=====================================");
    System.out.println();

    System.out.printf("Input bytes: %s\n", Arrays.toString(TEST_BYTES));
    System.out.printf("Hex representation: %s\n", bytesToHex(TEST_BYTES));
    System.out.println();

    // Test all methods
    System.out.println("Method Results (Big-Endian):");
    System.out.println("-----------------------------");

    long result1 = userCurrentApproach(TEST_BYTES, 0);
    System.out.printf("User's current approach:      %20d (0x%016X)\n", result1, result1);

    long result2 = stackOverflow60456641Approach(TEST_BYTES, 0);
    System.out.printf("StackOverflow 60456641:       %20d (0x%016X)\n", result2, result2);

    long result3 = stackOverflow29132118Loop(TEST_BYTES);
    System.out.printf("StackOverflow 29132118 Loop:  %20d (0x%016X)\n", result3, result3);

    long result4 = stackOverflow27610608Unrolled(TEST_BYTES);
    System.out.printf("StackOverflow 27610608:       %20d (0x%016X)\n", result4, result4);

    long result5 = byteBufferMethod(TEST_BYTES);
    System.out.printf("ByteBuffer Big-Endian:        %20d (0x%016X)\n", result5, result5);

    long result6 = bigIntegerMethod(TEST_BYTES);
    System.out.printf("BigInteger longValue():       %20d (0x%016X)\n", result6, result6);

    long result7 = bigIntegerExactMethod(TEST_BYTES);
    System.out.printf("BigInteger longValueExact():  %20d (0x%016X)\n", result7, result7);

    System.out.println();
    System.out.println("Little-Endian Comparison:");
    System.out.println("-------------------------");

    long result8 = unrolledLittleEndian(TEST_BYTES);
    System.out.printf("Unrolled Little-Endian:       %20d (0x%016X)\n", result8, result8);

    long result9 = byteBufferLittleEndian(TEST_BYTES);
    System.out.printf("ByteBuffer Little-Endian:     %20d (0x%016X)\n", result9, result9);

    System.out.println();

    // Validation
    boolean allBigEndianSame = result1 == result2 && result1 == result3 && result1 == result4 &&
        result1 == result5 && result1 == result6 && result1 == result7;
    boolean littleEndianSame = result8 == result9;

    if (allBigEndianSame) {
      System.out.println("✅ All big-endian methods produce the same result!");
    } else {
      System.out.println("❌ Big-endian methods produce different results!");
      System.out.println("   result1=" + result1 + ", result2=" + result2 + ", result3=" + result3);
      System.out.println("   result4=" + result4 + ", result5=" + result5 + ", result6=" + result6 + ", result7=" + result7);
    }

    if (littleEndianSame) {
      System.out.println("✅ All little-endian methods produce the same result!");
    } else {
      System.out.println("❌ Little-endian methods produce different results!");
    }

    System.out.println();
    System.out.println("📋 Notes:");
    System.out.println("• Big-endian vs Little-endian will produce different results");
    System.out.println("• Choose the endianness that matches your data format");
    System.out.println("• BigInteger methods handle arbitrary precision but may be slower");
    System.out.println("• longValueExact() throws ArithmeticException on overflow (safer)");
    System.out.println("• longValue() silently truncates on overflow (faster but potentially lossy)");

    // Demonstrate BigInteger overflow behavior with a larger number
    System.out.println();
    System.out.println("BigInteger Overflow Demonstration:");
    System.out.println("----------------------------------");

    // Create a BigInteger that's larger than Long.MAX_VALUE
    BigInteger largeBigInt = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(1000));
    System.out.printf("Large BigInteger: %s\n", largeBigInt.toString());
    System.out.printf("longValue() result: %d (truncated!)\n", largeBigInt.longValue());

    try {
      long exactResult = largeBigInt.longValueExact();
      System.out.printf("longValueExact() result: %d\n", exactResult);
    } catch (ArithmeticException e) {
      System.out.println("longValueExact() threw ArithmeticException (as expected for overflow)");
    }
    // test round trip conversion of byte array to long and back via ByteBuffer
    System.out.println();
    System.out.println("ByteBuffer Round Trip Conversion:");
    System.out.println("----------------------------------");

    long fromByteBuffer = byteBufferMethod(TEST_BYTES);
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES)
        .order(ByteOrder.BIG_ENDIAN)
        .putLong(fromByteBuffer);
    final var bytesInBuffer = buffer.array();
    if( Arrays.equals(TEST_BYTES, bytesInBuffer)){
      System.out.println("✅ Bytes Written matches original value!");
    } else {
      System.out.println("❌ Bytes Written does NOT match original value!");
      System.out.printf("Original bytes: %s\n", Arrays.toString(TEST_BYTES));
      System.out.printf("Bytes in buffer: %s\n", Arrays.toString(bytesInBuffer));
    }
    buffer.flip(); // Prepare for reading
    long roundTripResult = buffer.getLong();
    System.out.printf("Round trip conversion result: %20d (0x%016X)\n", roundTripResult, roundTripResult);
    if (roundTripResult == fromByteBuffer) {
      System.out.println("✅ Round trip conversion matches original value!");
    } else {
      System.out.println("❌ Round trip conversion does NOT match original value!");
    }
  }

  // Helper method to convert bytes to hex string
  private static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte b : bytes) {
      result.append(String.format("%02X ", b));
    }
    return result.toString().trim();
  }

  // All the conversion methods from the benchmark
  private static long userCurrentApproach(byte[] b, int offset) {
    long result = 0;
    for (int i = 0; i < 8; i++) {
      result |= ((long) b[offset + i] & 0xff) << (56 - (i * 8));
    }
    return result;
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

  private static long stackOverflow29132118Loop(byte[] b) {
    long result = 0;
    for (int i = 0; i < 8; i++) {
      result <<= 8;
      result |= (b[i] & 0xFF);
    }
    return result;
  }

  private static long stackOverflow27610608Unrolled(byte[] b) {
    return ((long) b[7] << 56)
        | ((long) b[6] & 0xff) << 48
        | ((long) b[5] & 0xff) << 40
        | ((long) b[4] & 0xff) << 32
        | ((long) b[3] & 0xff) << 24
        | ((long) b[2] & 0xff) << 16
        | ((long) b[1] & 0xff) << 8
        | ((long) b[0] & 0xff);
  }

  private static long byteBufferMethod(byte[] bytes) {
    return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
  }

  private static long bigIntegerMethod(byte[] bytes) {
    return new BigInteger(bytes).longValue();
  }

  private static long bigIntegerExactMethod(byte[] bytes) {
    return new BigInteger(bytes).longValueExact();
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

  private static long byteBufferLittleEndian(byte[] bytes) {
    return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
  }
}
