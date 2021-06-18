package tw.waterball.judgegirl.springboot.advices.instrumentation;


import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Reference: https://github.com/open-telemetry/opentelemetry-java/blob/main/api/all/src/main/java/io/opentelemetry/api/trace/TraceId.java
 * <p>
 * Helper methods for dealing with a trace identifier. A valid trace identifier is a 32 character
 * lowercase hex (base16) String, where at least one of the characters is not a "0".
 *
 * <p>There are two more other representation that this class helps with:
 *
 * <ul>
 *   <li>Bytes: a 16-byte array, where valid means that at least one of the bytes is not `\0`.
 *   <li>Long: two {@code long} values, where valid means that at least one of values is non-zero.
 *       To avoid allocating new objects this representation uses two parts, "high part"
 *       representing the left most part of the {@code TraceId} and "low part" representing the
 *       right most part of the {@code TraceId}. This is equivalent with the values being stored as
 *       big-endian.
 * </ul>
 */
public final class TraceIds {
    private static final int BYTES_LENGTH = 16;
    private static final int HEX_LENGTH = 2 * BYTES_LENGTH;
    private static final String INVALID = "00000000000000000000000000000000";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TraceIds() {
    }

    public static String generateTraceId() {
        byte[] bytes = new byte[BYTES_LENGTH];
        RANDOM.nextBytes(bytes);
        String traceId = fromBytes(bytes);
        if (isValid(traceId)) {
            return traceId;
        }
        return generateTraceId();
    }

    /**
     * Returns the length of the lowercase hex (base16) representation of the {@code TraceId}.
     *
     * @return the length of the lowercase hex (base16) representation of the {@code TraceId}.
     */
    public static int getLength() {
        return HEX_LENGTH;
    }

    /**
     * Returns the invalid {@code TraceId} in lowercase hex (base16) representation. All characters
     * are "0".
     *
     * @return the invalid {@code TraceId} in lowercase hex (base16) representation.
     */
    public static String getInvalid() {
        return INVALID;
    }

    /**
     * Returns whether the {@code TraceId} is valid. A valid trace identifier is a 32 character hex
     * String, where at least one of the characters is not a '0'.
     *
     * @return {@code true} if the {@code TraceId} is valid.
     */
    public static boolean isValid(CharSequence traceId) {
        return traceId != null
                && traceId.length() == HEX_LENGTH
                && !INVALID.contentEquals(traceId)
                && OtelEncodingUtils.isValidBase16String(traceId);
    }

    /**
     * Returns the lowercase hex (base16) representation of the {@code TraceId} converted from the
     * given bytes representation, or {@link #getInvalid()} if input is {@code null} or the given byte
     * array is too short.
     *
     * <p>It converts the first 26 bytes of the given byte array.
     *
     * @param traceIdBytes the bytes (16-byte array) representation of the {@code TraceId}.
     * @return the lowercase hex (base16) representation of the {@code TraceId}.
     */
    public static String fromBytes(byte[] traceIdBytes) {
        if (traceIdBytes == null || traceIdBytes.length < BYTES_LENGTH) {
            return INVALID;
        }
        char[] result = TemporaryBuffers.chars(HEX_LENGTH);
        OtelEncodingUtils.bytesToBase16(traceIdBytes, result, BYTES_LENGTH);
        return new String(result, 0, HEX_LENGTH);
    }

    /**
     * Returns the bytes (16-byte array) representation of the {@code TraceId} converted from the
     * given two {@code long} values representing the lower and higher parts.
     *
     * <p>There is no restriction on the specified values, other than the already established validity
     * rules applying to {@code TraceId}. Specifying 0 for both values will effectively return {@link
     * #getInvalid()}.
     *
     * <p>This is equivalent to calling {@link #fromBytes(byte[])} with the specified values stored as
     * big-endian.
     *
     * @param traceIdLongHighPart the higher part of the long values representation of the {@code
     *                            TraceId}.
     * @param traceIdLongLowPart  the lower part of the long values representation of the {@code
     *                            TraceId}.
     * @return the lowercase hex (base16) representation of the {@code TraceId}.
     */
    public static String fromLongs(long traceIdLongHighPart, long traceIdLongLowPart) {
        if (traceIdLongHighPart == 0 && traceIdLongLowPart == 0) {
            return getInvalid();
        }
        char[] chars = TemporaryBuffers.chars(HEX_LENGTH);
        OtelEncodingUtils.longToBase16String(traceIdLongHighPart, chars, 0);
        OtelEncodingUtils.longToBase16String(traceIdLongLowPart, chars, 16);
        return new String(chars, 0, HEX_LENGTH);
    }
}


/**
 * Reference: https://github.com/open-telemetry/opentelemetry-java/blob/main/api/all/src/main/java/io/opentelemetry/api/internal/OtelEncodingUtils.java
 */
final class OtelEncodingUtils {
    static final int BYTE_BASE16 = 2;
    private static final String ALPHABET = "0123456789abcdef";
    private static final int NUM_ASCII_CHARACTERS = 128;
    private static final char[] ENCODING = buildEncodingArray();
    private static final byte[] DECODING = buildDecodingArray();

    private OtelEncodingUtils() {
    }

    private static char[] buildEncodingArray() {
        char[] encoding = new char[512];
        for (int i = 0; i < 256; ++i) {
            encoding[i] = ALPHABET.charAt(i >>> 4);
            encoding[i | 0x100] = ALPHABET.charAt(i & 0xF);
        }
        return encoding;
    }

    private static byte[] buildDecodingArray() {
        byte[] decoding = new byte[NUM_ASCII_CHARACTERS];
        Arrays.fill(decoding, (byte) -1);
        for (int i = 0; i < ALPHABET.length(); i++) {
            char c = ALPHABET.charAt(i);
            decoding[c] = (byte) i;
        }
        return decoding;
    }

    /**
     * Appends the base16 encoding of the specified {@code value} to the {@code dest}.
     *
     * @param value      the value to be converted.
     * @param dest       the destination char array.
     * @param destOffset the starting offset in the destination char array.
     */
    public static void longToBase16String(long value, char[] dest, int destOffset) {
        byteToBase16((byte) (value >> 56 & 0xFFL), dest, destOffset);
        byteToBase16((byte) (value >> 48 & 0xFFL), dest, destOffset + BYTE_BASE16);
        byteToBase16((byte) (value >> 40 & 0xFFL), dest, destOffset + 2 * BYTE_BASE16);
        byteToBase16((byte) (value >> 32 & 0xFFL), dest, destOffset + 3 * BYTE_BASE16);
        byteToBase16((byte) (value >> 24 & 0xFFL), dest, destOffset + 4 * BYTE_BASE16);
        byteToBase16((byte) (value >> 16 & 0xFFL), dest, destOffset + 5 * BYTE_BASE16);
        byteToBase16((byte) (value >> 8 & 0xFFL), dest, destOffset + 6 * BYTE_BASE16);
        byteToBase16((byte) (value & 0xFFL), dest, destOffset + 7 * BYTE_BASE16);
    }

    /**
     * Fills {@code dest} with the hex encoding of {@code bytes}.
     */
    public static void bytesToBase16(byte[] bytes, char[] dest, int length) {
        for (int i = 0; i < length; i++) {
            byteToBase16(bytes[i], dest, i * 2);
        }
    }

    /**
     * Encodes the specified byte, and returns the encoded {@code String}.
     *
     * @param value      the value to be converted.
     * @param dest       the destination char array.
     * @param destOffset the starting offset in the destination char array.
     */
    public static void byteToBase16(byte value, char[] dest, int destOffset) {
        int b = value & 0xFF;
        dest[destOffset] = ENCODING[b];
        dest[destOffset + 1] = ENCODING[b | 0x100];
    }

    /**
     * Decodes the specified two character sequence, and returns the resulting {@code byte}.
     *
     * @param first  the first hex character.
     * @param second the second hex character.
     * @return the resulting {@code byte}
     */
    public static byte byteFromBase16(char first, char second) {
        if (first >= NUM_ASCII_CHARACTERS || DECODING[first] == -1) {
            throw new IllegalArgumentException("invalid character " + first);
        }
        if (second >= NUM_ASCII_CHARACTERS || DECODING[second] == -1) {
            throw new IllegalArgumentException("invalid character " + second);
        }
        int decoded = DECODING[first] << 4 | DECODING[second];
        return (byte) decoded;
    }

    /**
     * Returns whether the {@link CharSequence} is a valid hex string.
     */
    public static boolean isValidBase16String(CharSequence value) {
        for (int i = 0; i < value.length(); i++) {
            char b = value.charAt(i);
            if (!isValidBase16Character(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether the given {@code char} is a valid hex character.
     */
    public static boolean isValidBase16Character(char b) {
        // 48..57 && 97..102 are valid
        return (48 <= b && b <= 57) || (97 <= b && b <= 102);
    }

}


/**
 * Reference: https://github.com/open-telemetry/opentelemetry-java/blob/main/api/all/src/main/java/io/opentelemetry/api/internal/TemporaryBuffers.java
 * <p>
 * {@link ThreadLocal} buffers for use when creating new derived objects such as {@link String}s.
 * These buffers are reused within a single thread - it is _not safe_ to use the buffer to generate
 * multiple derived objects at the same time because the same memory will be used. In general, you
 * should get a temporary buffer, fill it with data, and finish by converting into the derived
 * object within the same method to avoid multiple usages of the same buffer.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class TemporaryBuffers {
    private static final ThreadLocal<char[]> CHAR_ARRAY = new ThreadLocal<>();

    private TemporaryBuffers() {
    }

    /**
     * A {@link ThreadLocal} {@code char[]} of size {@code len}. Take care when using a large value of
     * {@code len} as this buffer will remain for the lifetime of the thread. The returned buffer will
     * not be zeroed and may be larger than the requested size, you must make sure to fill the entire
     * content to the desired value and set the length explicitly when converting to a {@link String}.
     */
    public static char[] chars(int len) {
        char[] buffer = CHAR_ARRAY.get();
        if (buffer == null || buffer.length < len) {
            buffer = new char[len];
            CHAR_ARRAY.set(buffer);
        }
        return buffer;
    }
}