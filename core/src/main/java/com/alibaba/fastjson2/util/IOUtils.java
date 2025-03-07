package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;

import static com.alibaba.fastjson2.util.JDKUtils.*;

public class IOUtils {
    public static final int NULL_32 = BIG_ENDIAN ? 0x6e756c6c : 0x6c6c756e;
    public static final long NULL_64 = BIG_ENDIAN ? 0x6e0075006c006cL : 0x6c006c0075006eL;

    public static final int TRUE = BIG_ENDIAN ? 0x74727565 : 0x65757274;
    public static final long TRUE_64 = BIG_ENDIAN ? 0x74007200750065L : 0x65007500720074L;

    public static final int ALSE = BIG_ENDIAN ? 0x616c7365 : 0x65736c61;
    public static final long ALSE_64 = BIG_ENDIAN ? 0x61006c00730065L : 0x650073006c0061L;
    public static final long DOT_X0 = BIG_ENDIAN ? 0x2e00L : 0x2eL;

    public static final int INT_32_MULT_MIN_10 = Integer.MIN_VALUE / 10;
    public static final int INT_32_MULT_MIN_100 = Integer.MIN_VALUE / 100;

    public static final long INT_64_MULT_MIN_10 = Long.MIN_VALUE / 10;
    public static final long INT_64_MULT_MIN_100 = Long.MIN_VALUE / 100;

    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    public static final int[] DIGITS_K_32 = new int[1000];
    public static final long[] DIGITS_K_64 = new long[1000];

    private static final byte[] MIN_INT_BYTES = "-2147483648".getBytes();
    private static final char[] MIN_INT_CHARS = "-2147483648".toCharArray();
    private static final byte[] MIN_LONG = "-9223372036854775808".getBytes();

    public static final short[] PACKED_DIGITS;
    public static final int[] PACKED_DIGITS_UTF16;

    static final long[] POWER_TEN = {
            10,
            100,
            1000,
            10000,
            100000,
            1000000,
            10000000,
            100000000,
            1000000000,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L,
            10000000000000000L,
            100000000000000000L,
            1000000000000000000L,
    };

    static {
        short[] shorts = new short[]{
                0x3030, 0x3130, 0x3230, 0x3330, 0x3430, 0x3530, 0x3630, 0x3730, 0x3830, 0x3930,
                0x3031, 0x3131, 0x3231, 0x3331, 0x3431, 0x3531, 0x3631, 0x3731, 0x3831, 0x3931,
                0x3032, 0x3132, 0x3232, 0x3332, 0x3432, 0x3532, 0x3632, 0x3732, 0x3832, 0x3932,
                0x3033, 0x3133, 0x3233, 0x3333, 0x3433, 0x3533, 0x3633, 0x3733, 0x3833, 0x3933,
                0x3034, 0x3134, 0x3234, 0x3334, 0x3434, 0x3534, 0x3634, 0x3734, 0x3834, 0x3934,
                0x3035, 0x3135, 0x3235, 0x3335, 0x3435, 0x3535, 0x3635, 0x3735, 0x3835, 0x3935,
                0x3036, 0x3136, 0x3236, 0x3336, 0x3436, 0x3536, 0x3636, 0x3736, 0x3836, 0x3936,
                0x3037, 0x3137, 0x3237, 0x3337, 0x3437, 0x3537, 0x3637, 0x3737, 0x3837, 0x3937,
                0x3038, 0x3138, 0x3238, 0x3338, 0x3438, 0x3538, 0x3638, 0x3738, 0x3838, 0x3938,
                0x3039, 0x3139, 0x3239, 0x3339, 0x3439, 0x3539, 0x3639, 0x3739, 0x3839, 0x3939
        };
        int[] digits = new int[]{
                0x300030, 0x310030, 0x320030, 0x330030, 0x340030, 0x350030, 0x360030, 0x370030, 0x380030, 0x390030,
                0x300031, 0x310031, 0x320031, 0x330031, 0x340031, 0x350031, 0x360031, 0x370031, 0x380031, 0x390031,
                0x300032, 0x310032, 0x320032, 0x330032, 0x340032, 0x350032, 0x360032, 0x370032, 0x380032, 0x390032,
                0x300033, 0x310033, 0x320033, 0x330033, 0x340033, 0x350033, 0x360033, 0x370033, 0x380033, 0x390033,
                0x300034, 0x310034, 0x320034, 0x330034, 0x340034, 0x350034, 0x360034, 0x370034, 0x380034, 0x390034,
                0x300035, 0x310035, 0x320035, 0x330035, 0x340035, 0x350035, 0x360035, 0x370035, 0x380035, 0x390035,
                0x300036, 0x310036, 0x320036, 0x330036, 0x340036, 0x350036, 0x360036, 0x370036, 0x380036, 0x390036,
                0x300037, 0x310037, 0x320037, 0x330037, 0x340037, 0x350037, 0x360037, 0x370037, 0x380037, 0x390037,
                0x300038, 0x310038, 0x320038, 0x330038, 0x340038, 0x350038, 0x360038, 0x370038, 0x380038, 0x390038,
                0x300039, 0x310039, 0x320039, 0x330039, 0x340039, 0x350039, 0x360039, 0x370039, 0x380039, 0x390039
        };

        if (BIG_ENDIAN) {
            for (int i = 0; i < shorts.length; i++) {
                shorts[i] = Short.reverseBytes(shorts[i]);
            }
            for (int i = 0; i < digits.length; i++) {
                digits[i] = Integer.reverseBytes(digits[i] << 8);
            }
        }
        PACKED_DIGITS = shorts;
        PACKED_DIGITS_UTF16 = digits;

        for (int i = 0; i < 1000; i++) {
            int c0 = i < 10 ? 2 : i < 100 ? 1 : 0;
            int c1 = (i / 100) + '0';
            int c2 = ((i / 10) % 10) + '0';
            int c3 = i % 10 + '0';
            DIGITS_K_32[i] = c0 + (c1 << 8) + (c2 << 16) + (c3 << 24);
            long v = (c1 << 16) + (((long) c2) << 32) + (((long) c3) << 48);
            if (BIG_ENDIAN) {
                v <<= 8;
            }
            DIGITS_K_64[i] = c0 + v;
        }
    }

    public static int stringSize(int x) {
        for (int i = 0; ; i++) {
            if (x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    public static int stringSize(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p) {
                return i;
            }
            p = 10 * p;
        }
        return 19;
    }

    public static void getChars(int i, int index, byte[] buf) {
        int q, r;
        int charPos = index;

        boolean negative = i < 0;
        if (!negative) {
            i = -i;
        }

        // Generate two digits per iteration
        while (i <= -100) {
            q = i / 100;
            r = (q * 100) - i;
            i = q;
            charPos -= 2;
            UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + charPos, PACKED_DIGITS[r]);
        }

        // We know there are at most two digits left at this point.
        if (i < -9) {
            charPos -= 2;
            UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + charPos, PACKED_DIGITS[-i]);
        } else {
            buf[--charPos] = (byte) ('0' - i);
        }

        if (negative) {
            buf[charPos - 1] = (byte) '-';
        }
    }

    public static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;

        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }

        // Get 2 digits/iteration using ints
        while (i <= -100) {
            q = i / 100;
            r = (q * 100) - i;
            i = q;

            charPos -= 2;
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[r]);
        }

        // We know there are at most two digits left at this point.
        if (i < -9) {
            charPos -= 2;
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[-i]);
        } else {
            buf[--charPos] = (char) ('0' - i);
        }

        if (negative) {
            buf[charPos - 1] = '-';
        }
    }

    public static void getChars(long i, int index, byte[] buf) {
        long q;
        int charPos = index;

        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i <= Integer.MIN_VALUE) {
            q = i / 100;
            charPos -= 2;
            UNSAFE.putShort(
                    buf,
                    ARRAY_BYTE_BASE_OFFSET + charPos,
                    PACKED_DIGITS[(int) ((q * 100) - i)]);
            i = q;
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 <= -100) {
            q2 = i2 / 100;
            charPos -= 2;
            UNSAFE.putShort(
                    buf,
                    ARRAY_BYTE_BASE_OFFSET + charPos,
                    PACKED_DIGITS[(q2 * 100) - i2]);
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 < -9) {
            charPos -= 2;
            UNSAFE.putShort(
                    buf,
                    ARRAY_BYTE_BASE_OFFSET + charPos,
                    PACKED_DIGITS[-i2]);
        } else {
            buf[--charPos] = (byte) ('0' - i2);
        }

        if (negative) {
            buf[charPos - 1] = (byte) '-';
        }
    }

    public static void getChars(long i, int index, char[] buf) {
        long q;
        int charPos = index;

        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i <= Integer.MIN_VALUE) {
            q = i / 100;
            charPos -= 2;
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[(int) ((q * 100) - i)]);
            i = q;
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 <= -100) {
            q2 = i2 / 100;
            charPos -= 2;
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[(q2 * 100) - i2]);
            i2 = q2;
        }

        // We know there are at most two digits left at this point.
        if (i2 < -9) {
            charPos -= 2;
            UNSAFE.putInt(
                    buf,
                    ARRAY_CHAR_BASE_OFFSET + (charPos << 1),
                    PACKED_DIGITS_UTF16[-i2]);
        } else {
            buf[--charPos] = (char) ('0' - i2);
        }

        if (negative) {
            buf[--charPos] = '-';
        }
    }

    public static int writeDecimal(byte[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            buf[off++] = (byte) '-';
            unscaledVal = -unscaledVal;
        }

        if (scale != 0) {
            int unscaleValSize = IOUtils.stringSize(unscaledVal);
            int insertionPoint = unscaleValSize - scale;
            if (insertionPoint == 0) {
                buf[off] = '0';
                buf[off + 1] = '.';
                off += 2;
            } else if (insertionPoint < 0) {
                buf[off] = '0';
                buf[off + 1] = '.';
                off += 2;

                for (int i = 0; i < -insertionPoint; i++) {
                    buf[off++] = '0';
                }
            } else {
                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                off = IOUtils.writeInt64(buf, off, div);
                buf[off] = '.';

                if (scale == 1) {
                    buf[off + 1] = (byte) (rem + '0');
                    return off + 2;
                } else if (scale == 2) {
                    UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + off + 1, PACKED_DIGITS[(int) rem]);
                    return off + 3;
                }

                for (int i = 0, end = unscaleValSize - stringSize(rem) - insertionPoint; i < end; ++i) {
                    buf[++off] = '0';
                }
                return IOUtils.writeInt64(buf, off + 1, rem);
            }
        }

        return IOUtils.writeInt64(buf, off, unscaledVal);
    }

    public static int writeDecimal(char[] buf, int off, long unscaledVal, int scale) {
        if (unscaledVal < 0) {
            buf[off++] = (byte) '-';
            unscaledVal = -unscaledVal;
        }

        if (scale != 0) {
            int unscaleValSize = stringSize(unscaledVal);
            int insertionPoint = unscaleValSize - scale;
            if (insertionPoint == 0) {
                buf[off] = '0';
                buf[off + 1] = '.';
                off += 2;
            } else if (insertionPoint < 0) {
                buf[off] = '0';
                buf[off + 1] = '.';
                off += 2;

                for (int i = 0; i < -insertionPoint; i++) {
                    buf[off++] = '0';
                }
            } else {
                long power = POWER_TEN[scale - 1];
                long div = unscaledVal / power;
                long rem = unscaledVal - div * power;
                off = IOUtils.writeInt64(buf, off, div);
                buf[off] = '.';

                if (scale == 1) {
                    buf[off + 1] = (char) (rem + '0');
                    return off + 2;
                } else if (scale == 2) {
                    UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((off + 1) << 1), PACKED_DIGITS_UTF16[(int) rem]);
                    return off + 3;
                }

                for (int i = 0, end = unscaleValSize - stringSize(rem) - insertionPoint; i < end; ++i) {
                    buf[++off] = '0';
                }
                return IOUtils.writeInt64(buf, off + 1, rem);
            }
        }

        return IOUtils.writeInt64(buf, off, unscaledVal);
    }

    public static int encodeUTF8(byte[] src, int offset, int len, byte[] dst, int dp) {
        int sl = offset + len;
        while (offset < sl) {
            char c = UNSAFE.getChar(src, ARRAY_BYTE_BASE_OFFSET + offset);
            offset += 2;

            if (c < 0x80) {
                dst[dp++] = (byte) c;
            } else {
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    dst[dp] = (byte) (0xc0 | (c >> 6));
                    dst[dp + 1] = (byte) (0x80 | (c & 0x3f));
                    dp += 2;
                } else if (c >= '\uD800' && c <= '\uDFFF') {
                    utf8_char2(src, offset, sl, c, dst, dp);
                    offset += 2;
                    dp += 4;
                } else {
                    // 3 bytes, 16 bits
                    dst[dp] = (byte) (0xe0 | ((c >> 12)));
                    dst[dp + 1] = (byte) (0x80 | ((c >> 6) & 0x3f));
                    dst[dp + 2] = (byte) (0x80 | (c & 0x3f));
                    dp += 3;
                }
            }
        }
        return dp;
    }

    public static int encodeUTF8(char[] src, int offset, int len, byte[] dst, int dp) {
        int sl = offset + len;
        int dlASCII = dp + Math.min(len, dst.length);

        // ASCII only optimized loop
        while (dp < dlASCII && src[offset] < '\u0080') {
            dst[dp++] = (byte) src[offset++];
        }

        while (offset < sl) {
            char c = src[offset++];
            if (c < 0x80) {
                // Have at most seven bits
                dst[dp++] = (byte) c;
            } else if (c < 0x800) {
                // 2 bytes, 11 bits
                dst[dp] = (byte) (0xc0 | (c >> 6));
                dst[dp + 1] = (byte) (0x80 | (c & 0x3f));
                dp += 2;
            } else if (c >= '\uD800' && c <= '\uDFFF') {
                utf8_char2(src, offset, sl, c, dst, dp);
                offset++;
                dp += 4;
            } else {
                // 3 bytes, 16 bits
                dst[dp] = (byte) (0xe0 | ((c >> 12)));
                dst[dp + 1] = (byte) (0x80 | ((c >> 6) & 0x3f));
                dst[dp + 2] = (byte) (0x80 | (c & 0x3f));
                dp += 3;
            }
        }
        return dp;
    }

    private static void utf8_char2(byte[] src, int offset, int sl, char c, byte[] dst, int dp) {
        char d;
        if (c > '\uDBFF'
                || sl - offset < 1
                || (d = UNSAFE.getChar(src, ARRAY_BYTE_BASE_OFFSET + offset)) < '\uDC00'
                || d > '\uDFFF'
        ) {
            throw new JSONException("malformed input off : " + offset);
        }

        int uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00');
        dst[dp] = (byte) (0xf0 | ((uc >> 18)));
        dst[dp + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
        dst[dp + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
        dst[dp + 3] = (byte) (0x80 | (uc & 0x3f));
    }

    private static void utf8_char2(char[] src, int offset, int sl, char c, byte[] dst, int dp) {
        char d;
        if (c > '\uDBFF' || sl - offset < 1 || (d = src[offset]) < '\uDC00' || d > '\uDFFF') {
            throw new JSONException("malformed input off : " + offset);
        }

        int uc = ((c << 10) + d) + (0x010000 - ('\uD800' << 10) - '\uDC00');
        dst[dp] = (byte) (0xf0 | ((uc >> 18)));
        dst[dp + 1] = (byte) (0x80 | ((uc >> 12) & 0x3f));
        dst[dp + 2] = (byte) (0x80 | ((uc >> 6) & 0x3f));
        dst[dp + 3] = (byte) (0x80 | (uc & 0x3f));
    }

    public static boolean isNumber(String str) {
        for (int i = 0; i < str.length(); ++i) {
            char ch = str.charAt(i);
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(char[] chars, int off, int len) {
        for (int i = off, end = off + len; i < end; ++i) {
            char ch = chars[i];
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumber(byte[] chars, int off, int len) {
        for (int i = off, end = off + len; i < end; ++i) {
            char ch = (char) chars[i];
            if (ch == '+' || ch == '-') {
                if (i != 0) {
                    return false;
                }
            } else if (ch < '0' || ch > '9') {
                return false;
            }
        }
        return true;
    }

    public static void close(Closeable x) {
        if (x == null) {
            return;
        }

        try {
            x.close();
        } catch (Exception e) {
            // skip
        }
    }

    public static int decodeUTF8(byte[] src, int off, int len, byte[] dst) {
        final int sl = off + len;
        int dp = 0;

        while (off < sl) {
            int b0 = src[off++];
            if (b0 >= 0) {
                // 1 byte, 7 bits: 0xxxxxxx
                dst[dp] = (byte) b0;
                dst[dp + 1] = 0;
                dp += 2;
            } else if ((b0 >> 5) == -2 && (b0 & 0x1e) != 0) {
                // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
                if (off < sl) {
                    int b1 = src[off++];
                    if ((b1 & 0xc0) != 0x80) { // isNotContinuation(b2)
                        return -1;
                    } else {
                        char c = (char) (((b0 << 6) ^ b1) ^
                                (((byte) 0xC0 << 6) ^
                                        ((byte) 0x80)));
                        dst[dp] = (byte) c;
                        dst[dp + 1] = (byte) (c >> 8);
                        dp += 2;
                    }
                    continue;
                }
                dst[dp] = (byte) b0;
                dst[dp + 1] = 0;
                dp += 2;
                break;
            } else if ((b0 >> 4) == -2) {
                // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                if (off + 1 < sl) {
                    int b1 = src[off];
                    int b2 = src[off + 1];
                    off += 2;
                    if ((b0 == (byte) 0xe0 && (b1 & 0xe0) == 0x80) //
                            || (b1 & 0xc0) != 0x80 //
                            || (b2 & 0xc0) != 0x80) { // isMalformed3(b0, b1, b2)
                        return -1;
                    } else {
                        char c = (char)
                                ((b0 << 12) ^
                                        (b1 << 6) ^
                                        (b2 ^ (((byte) 0xE0 << 12) ^
                                                ((byte) 0x80 << 6) ^
                                                ((byte) 0x80)))
                                );
                        boolean isSurrogate = c >= '\uD800' && c < ('\uDFFF' + 1);
                        if (isSurrogate) {
                            return -1;
                        } else {
                            dst[dp] = (byte) c;
                            dst[dp + 1] = (byte) (c >> 8);
                            dp += 2;
                        }
                    }
                    continue;
                }
                return -1;
            } else if ((b0 >> 3) == -2) {
                // 4 bytes, 21 bits: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                if (off + 2 < sl) {
                    int b2 = src[off];
                    int b3 = src[off + 1];
                    int b4 = src[off + 2];
                    off += 3;
                    int uc = ((b0 << 18) ^
                            (b2 << 12) ^
                            (b3 << 6) ^
                            (b4 ^
                                    (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80))));
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80) // isMalformed4
                            ||
                            // shortest form check
                            !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                    ) {
                        return -1;
                    } else {
                        char c = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10)));
                        dst[dp] = (byte) c;
                        dst[dp + 1] = (byte) (c >> 8);
                        dp += 2;

                        c = (char) ((uc & 0x3ff) + '\uDC00');
                        dst[dp] = (byte) c;
                        dst[dp + 1] = (byte) (c >> 8);
                        dp += 2;
                    }
                    continue;
                }
                return -1;
            } else {
                return -1;
            }
        }
        return dp;
    }

    public static int decodeUTF8(byte[] src, int off, int len, char[] dst) {
        final int sl = off + len;
        int dp = 0;
        int dlASCII = Math.min(len, dst.length);

        // ASCII only optimized loop
        while (dp < dlASCII && src[off] >= 0) {
            dst[dp++] = (char) src[off++];
        }

        while (off < sl) {
            int b1 = src[off++];
            if (b1 >= 0) {
                // 1 byte, 7 bits: 0xxxxxxx
                dst[dp++] = (char) b1;
            } else if ((b1 >> 5) == -2 && (b1 & 0x1e) != 0) {
                // 2 bytes, 11 bits: 110xxxxx 10xxxxxx
                if (off < sl) {
                    int b2 = src[off++];
                    if ((b2 & 0xc0) != 0x80) { // isNotContinuation(b2)
                        return -1;
                    } else {
                        dst[dp++] = (char) (((b1 << 6) ^ b2) ^
                                (((byte) 0xC0 << 6) ^
                                        ((byte) 0x80)));
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 4) == -2) {
                // 3 bytes, 16 bits: 1110xxxx 10xxxxxx 10xxxxxx
                if (off + 1 < sl) {
                    int b2 = src[off];
                    int b3 = src[off + 1];
                    off += 2;
                    if ((b1 == (byte) 0xe0 && (b2 & 0xe0) == 0x80) //
                            || (b2 & 0xc0) != 0x80 //
                            || (b3 & 0xc0) != 0x80) { // isMalformed3(b1, b2, b3)
                        return -1;
                    } else {
                        char c = (char) ((b1 << 12) ^
                                (b2 << 6) ^
                                (b3 ^
                                        (((byte) 0xE0 << 12) ^
                                                ((byte) 0x80 << 6) ^
                                                ((byte) 0x80))));
                        boolean isSurrogate = c >= '\uD800' && c < ('\uDFFF' + 1);
                        if (isSurrogate) {
                            return -1;
                        } else {
                            dst[dp++] = c;
                        }
                    }
                    continue;
                }
                return -1;
            } else if ((b1 >> 3) == -2) {
                // 4 bytes, 21 bits: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
                if (off + 2 < sl) {
                    int b2 = src[off];
                    int b3 = src[off + 1];
                    int b4 = src[off + 2];
                    off += 3;
                    int uc = ((b1 << 18) ^
                            (b2 << 12) ^
                            (b3 << 6) ^
                            (b4 ^
                                    (((byte) 0xF0 << 18) ^
                                            ((byte) 0x80 << 12) ^
                                            ((byte) 0x80 << 6) ^
                                            ((byte) 0x80))));
                    if (((b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 || (b4 & 0xc0) != 0x80) // isMalformed4
                            ||
                            // shortest form check
                            !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
                    ) {
                        return -1;
                    } else {
                        dst[dp] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                        dst[dp + 1] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
                        dp += 2;
                    }
                    continue;
                }
                return -1;
            } else {
                return -1;
            }
        }
        return dp;
    }

    public static long lines(File file) throws Exception {
        try (FileInputStream in = new FileInputStream(file)) {
            return lines(in);
        }
    }

    public static long lines(InputStream in) throws Exception {
        long lines = 0;
        byte[] buf = new byte[1024 * 8];
        while (true) {
            int len = in.read(buf, 0, buf.length);
            if (len == -1) {
                break;
            }
            for (int i = 0; i < len; i++) {
                byte b = buf[i];
                if (b == '\n') {
                    lines++;
                }
            }
        }
        return lines;
    }

    public static int writeLocalDate(byte[] bytes, int off, int year, int month, int dayOfMonth) {
        if (year < 0) {
            bytes[off++] = '-';
            year = -year;
        } else if (year > 9999) {
            bytes[off++] = '+';
        }

        if (year < 10000) {
            int y01 = year / 100;
            int y23 = year - y01 * 100;
            UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off, PACKED_DIGITS[y01]);
            UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 2, PACKED_DIGITS[y23]);
            off += 4;
        } else {
            off = IOUtils.writeInt32(bytes, off, year);
        }

        bytes[off] = '-';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 1, PACKED_DIGITS[month]);
        bytes[off + 3] = '-';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 4, PACKED_DIGITS[dayOfMonth]);
        return off + 6;
    }

    public static int writeLocalDate(char[] chars, int off, int year, int month, int dayOfMonth) {
        if (year < 0) {
            chars[off++] = '-';
            year = -year;
        } else if (year > 9999) {
            chars[off++] = '+';
        }

        if (year < 10000) {
            int y01 = year / 100;
            int y23 = year - y01 * 100;
            UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (off << 1), PACKED_DIGITS_UTF16[y01]);
            UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 2) << 1), PACKED_DIGITS_UTF16[y23]);
            off += 4;
        } else {
            off = IOUtils.writeInt32(chars, off, year);
        }

        chars[off] = '-';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 1) << 1), PACKED_DIGITS_UTF16[month]);
        chars[off + 3] = '-';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 4) << 1), PACKED_DIGITS_UTF16[dayOfMonth]);
        return off + 6;
    }

    public static void writeLocalTime(byte[] bytes, int off, int hour, int minute, int second) {
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off, PACKED_DIGITS[hour]);
        bytes[off + 2] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 3, PACKED_DIGITS[minute]);
        bytes[off + 5] = ':';
        UNSAFE.putShort(bytes, ARRAY_BYTE_BASE_OFFSET + off + 6, PACKED_DIGITS[second]);
    }

    public static int writeLocalTime(byte[] bytes, int off, LocalTime time) {
        writeLocalTime(bytes, off, time.getHour(), time.getMinute(), time.getSecond());
        off += 8;

        int nano = time.getNano();
        return nano != 0 ? writeNano(bytes, off, nano) : off;
    }

    public static int writeNano(byte[] bytes, int off, int nano) {
        final int div = nano / 1000;
        final int div2 = div / 1000;
        final int rem1 = nano - div * 1000;

        putInt(bytes, off, DIGITS_K_32[div2] & 0xffffff00 | '.');
        off += 4;

        int v;
        if (rem1 == 0) {
            final int rem2 = div - div2 * 1000;
            if (rem2 == 0) {
                return off;
            }

            v = DIGITS_K_32[rem2];
        } else {
            v = DIGITS_K_32[div - div2 * 1000];
        }

        bytes[off] = (byte) (v >> 8);
        bytes[off + 1] = (byte) (v >> 16);
        off += 2;
        if (rem1 == 0) {
            bytes[off] = (byte) (v >> 24);
            return off + 1;
        }

        putInt(bytes, off, DIGITS_K_32[rem1] & 0xffffff00 | (v >> 24));
        return off + 4;
    }

    public static int writeNano(char[] chars, int off, int nano) {
        final int div = nano / 1000;
        final int div2 = div / 1000;
        final int rem1 = nano - div * 1000;

        putLong(chars, off, DIGITS_K_64[div2] & 0xffffffffffff0000L | DOT_X0);
        off += 4;

        long v;
        if (rem1 == 0) {
            final int rem2 = div - div2 * 1000;
            if (rem2 == 0) {
                return off;
            }

            v = DIGITS_K_64[rem2];
        } else {
            v = DIGITS_K_64[div - div2 * 1000];
        }

        chars[off] = (char) (v >> 16);
        chars[off + 1] = (char) (v >> 32);
        off += 2;
        if (rem1 == 0) {
            chars[off] = (char) (v >> 48);
            return off + 1;
        }

        putLong(chars, off, DIGITS_K_64[rem1] & 0xffffffffffff0000L | (v >> 48));
        return off + 4;
    }

    public static void writeLocalTime(char[] chars, int off, int hour, int minute, int second) {
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + (off << 1), PACKED_DIGITS_UTF16[hour]);
        chars[off + 2] = ':';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 3) << 1), PACKED_DIGITS_UTF16[minute]);
        chars[off + 5] = ':';
        UNSAFE.putInt(chars, ARRAY_CHAR_BASE_OFFSET + ((off + 6) << 1), PACKED_DIGITS_UTF16[second]);
    }

    public static int writeLocalTime(char[] chars, int off, LocalTime time) {
        writeLocalTime(chars, off, time.getHour(), time.getMinute(), time.getSecond());
        off += 8;

        int nano = time.getNano();
        return nano != 0 ? writeNano(chars, off, nano) : off;
    }

    public static int writeInt64(final byte[] buf, int pos, final long value) {
        long i;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                System.arraycopy(MIN_LONG, 0, buf, pos, MIN_LONG.length);
                return pos + MIN_LONG.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K_32[(int) i];
            int start = v & 0xff;
            if (start == 0) {
                buf[pos] = (byte) (v >> 8);
                buf[pos + 1] = (byte) (v >> 16);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 16);
            }
            buf[pos++] = (byte) (v >> 24);
            return pos;
        }

        final long q1 = i / 1000;
        final int r1 = (int) (i - q1 * 1000);
        final int v1 = DIGITS_K_32[r1];
        if (i < 1000000) {
            final int v2 = DIGITS_K_32[(int) q1];

            int start = v2 & 0xff;
            if (start == 0) {
                buf[pos] = (byte) (v2 >> 8);
                buf[pos + 1] = (byte) (v2 >> 16);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v2 >> 16);
            }
            putInt(buf, pos, v1 & 0xffffff00 | (v2 >> 24));
            return pos + 4;
        }

        final long q2 = q1 / 1000;
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        final int v2 = DIGITS_K_32[r2];
        if (q3 == 0) {
            final int v3 = DIGITS_K_32[(int) q2];
            int start = v3 & 0xff;
            if (start == 0) {
                buf[pos] = (byte) (v3 >> 8);
                buf[pos + 1] = (byte) (v3 >> 16);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v3 >> 16);
            }
            buf[pos] = (byte) (v3 >> 24);
            buf[pos + 1] = (byte) (v2 >> 8);
            buf[pos + 2] = (byte) (v2 >> 16);
            putInt(buf, pos + 3, v1 & 0xffffff00 | (v2 >> 24));
            return pos + 7;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        final int v3 = DIGITS_K_32[r3];
        if (q4 == 0) {
            final int v4 = DIGITS_K_32[(int) q3];
            final int start = v4 & 0xff;
            if (start == 0) {
                buf[pos] = (byte) (v4 >> 8);
                buf[pos + 1] = (byte) (v4 >> 16);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v4 >> 16);
            }
            buf[pos] = (byte) (v4 >> 24);
            buf[pos + 1] = (byte) (v3 >> 8);
            putInt(buf, pos + 2, ((v2 & 0x00ffff00) << 8) | (v3 >> 16));
            putInt(buf, pos + 6, v1 & 0xffffff00 | (v2 >> 24));
            return pos + 10;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;

        final int v4 = DIGITS_K_32[r4];
        if (q5 == 0) {
            final int v5 = DIGITS_K_32[q4];
            int start = v5 & 0xff;
            if (start == 0) {
                buf[pos] = (byte) (v5 >> 8);
                buf[pos + 1] = (byte) (v5 >> 16);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v5 >> 16);
            }
            putInt(buf, pos, v4 & 0xffffff00 | (v5 >> 24));
            buf[pos + 4] = (byte) (v3 >> 8);
            putInt(buf, pos + 5, ((v2 & 0x00ffff00) << 8) | (v3 >> 16));
            putInt(buf, pos + 9, v1 & 0xffffff00 | (v2 >> 24));
            return pos + 13;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final int v5 = DIGITS_K_32[r5];
        if (q6 == 0) {
            int v = DIGITS_K_32[q5];
            final int start = v & 0xff;
            if (start == 0) {
                buf[pos] = (byte) (v >> 8);
                buf[pos + 1] = (byte) (v >> 16);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 16);
            }
            buf[pos++] = (byte) (v >> 24);
        } else {
            putInt(buf, pos, DIGITS_K_32[q5 - q6 * 1000] & 0xffffff00 | (q6 + '0'));
            pos += 4;
        }

        buf[pos] = (byte) (v5 >> 8);
        putInt(buf, pos + 1, ((v4 & 0x00ffff00) << 8) | (v5 >> 16));
        putInt(buf, pos + 5, v3 & 0xffffff00 | (v4 >> 24));
        buf[pos + 9] = (byte) (v2 >> 8);
        buf[pos + 10] = (byte) (v2 >> 16);
        putInt(buf, pos + 11, v1 & 0xffffff00 | (v2 >> 24));
        return pos + 15;
    }

    public static int writeInt64(final char[] buf, int pos, final long value) {
        long i;
        if (value < 0) {
            if (value == Long.MIN_VALUE) {
                for (int x = 0; x < MIN_LONG.length; x++) {
                    buf[pos + x] = (char) MIN_LONG[x];
                }
                return pos + MIN_LONG.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            long v = DIGITS_K_64[(int) i];
            int start = (byte) v;
            if (start == 0) {
                putInt(buf, pos, (int) (v >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v >> 32);
            }
            buf[pos++] = (char) (v >> 48);
            return pos;
        }

        final long q1 = i / 1000;
        final int r1 = (int) (i - q1 * 1000);
        final long v1 = DIGITS_K_64[r1];
        if (i < 1000000) {
            final long v2 = DIGITS_K_64[(int) q1];
            int start = (byte) v2;
            if (start == 0) {
                putInt(buf, pos, (int) (v2 >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v2 >> 32);
            }
            putLong(buf, pos, v1 & 0xffffffffffff0000L | (v2 >> 48));
            return pos + 4;
        }

        final long q2 = q1 / 1000;
        final int r2 = (int) (q1 - q2 * 1000);
        final long q3 = q2 / 1000;
        final long v2 = DIGITS_K_64[r2];
        if (q3 == 0) {
            final long v3 = DIGITS_K_64[(int) q2];
            int start = (byte) v3;
            if (start == 0) {
                putInt(buf, pos, (int) (v3 >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v3 >> 32);
            }
            buf[pos] = (char) (v3 >> 48);
            putInt(buf, pos + 1, (int) (v2 >> 16));
            putLong(buf, pos + 3, v1 & 0xffffffffffff0000L | (v2 >> 48));
            return pos + 7;
        }
        final int r3 = (int) (q2 - q3 * 1000);
        final int q4 = (int) (q3 / 1000);
        final long v3 = DIGITS_K_64[r3];
        if (q4 == 0) {
            final long v4 = DIGITS_K_64[(int) q3];
            final int start = (byte) v4;
            if (start == 0) {
                putInt(buf, pos, (int) (v4 >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v4 >> 32);
            }
            buf[pos] = (char) (v4 >> 48);
            buf[pos + 1] = (char) (v3 >> 16);
            putLong(buf, pos + 2, ((v2 & 0x0000ffffffff0000L) << 16) | (v3 >> 32));
            putLong(buf, pos + 6, v1 & 0xffffffffffff0000L | (v2 >> 48));
            return pos + 10;
        }
        final int r4 = (int) (q3 - q4 * 1000);
        final int q5 = q4 / 1000;
        final long v4 = DIGITS_K_64[r4];
        if (q5 == 0) {
            final long v5 = DIGITS_K_64[q4];
            int start = (byte) v5;
            if (start == 0) {
                putInt(buf, pos, (int) (v5 >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v5 >> 32);
            }
            buf[pos] = (char) (v5 >> 48);
            putInt(buf, pos + 1, (int) (v4 >> 16));
            putLong(buf, pos + 3, v3 & 0xffffffffffff0000L | (v4 >> 48));
            putInt(buf, pos + 7, (int) (v2 >> 16));
            putLong(buf, pos + 9, v1 & 0xffffffffffff0000L | (v2 >> 48));
            return pos + 13;
        }
        final int r5 = q4 - q5 * 1000;
        final int q6 = q5 / 1000;
        final long v5 = DIGITS_K_64[r5];
        if (q6 == 0) {
            int v = DIGITS_K_32[q5];
            final int start = (byte) v;
            if (start == 0) {
                buf[pos] = (char) (byte) (v >> 8);
                buf[pos + 1] = (char) (byte) (v >> 16);
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (byte) (v >> 16);
            }
            buf[pos++] = (char) (v >> 24);
        } else {
            putLong(buf, pos, DIGITS_K_64[q5 - q6 * 1000]);
            buf[pos] = (char) (q6 + '0');
            pos += 4;
        }

        putInt(buf, pos, (int) (v5 >> 16));
        putLong(buf, pos + 2, v4 & 0xffffffffffff0000L | (v5 >> 48));

        buf[pos + 6] = (char) (v3 >> 16);
        putLong(buf, pos + 7, ((v2 & 0x0000ffffffff0000L) << 16) | (v3 >> 32));
        putLong(buf, pos + 11, v1 & 0xffffffffffff0000L | (v2 >> 48));
        return pos + 15;
    }

    public static int writeInt8(final byte[] buf, int pos, final byte value) {
        int i;
        if (value < 0) {
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        int v = DIGITS_K_32[i];
        final int start = (byte) v;
        if (start == 0) {
            putShort(buf, pos, (short) (v >> 8));
            pos += 2;
        } else if (start == 1) {
            buf[pos++] = (byte) (v >> 16);
        }
        buf[pos] = (byte) (v >> 24);
        return pos + 1;
    }

    public static int writeInt8(final char[] buf, int pos, final byte value) {
        int i;
        if (value < 0) {
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        long v = DIGITS_K_64[i];
        final int start = (byte) v;
        if (start == 0) {
            putInt(buf, pos, (int) (v >> 16));
            pos += 2;
        } else if (start == 1) {
            buf[pos++] = (char) (v >> 32);
        }
        buf[pos] = (char) (v >> 48);
        return pos + 1;
    }

    public static int writeInt16(final byte[] buf, int pos, final short value) {
        int i;
        if (value < 0) {
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K_32[i];
            final int start = (byte) v;
            if (start == 0) {
                putShort(buf, pos, (short) (v >> 8));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 16);
            }
            buf[pos] = (byte) (v >> 24);
            return pos + 1;
        }

        final int q1 = i / 1000;
        final int v2 = DIGITS_K_32[q1];
        if ((byte) v2 == 1) {
            buf[pos++] = (byte) (v2 >> 16);
        }
        putInt(buf, pos, (DIGITS_K_32[i - q1 * 1000]) & 0xffffff00 | (v2 >> 24));
        return pos + 4;
    }

    public static int writeInt16(final char[] buf, int pos, final short value) {
        int i;
        if (value < 0) {
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            long v = DIGITS_K_64[i];
            final int start = (byte) v;
            if (start == 0) {
                putInt(buf, pos, (int) (v >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v >> 32);
            }
            buf[pos] = (char) (v >> 48);
            return pos + 1;
        }

        final int q1 = i / 1000;
        final long v2 = DIGITS_K_64[q1];
        if ((byte) v2 == 1) {
            buf[pos++] = (char) (v2 >> 32);
        }
        putLong(buf, pos, DIGITS_K_64[i - q1 * 1000] & 0xffffffffffff0000L | (v2 >> 48));
        return pos + 4;
    }

    public static int writeInt32(final byte[] buf, int pos, final int value) {
        int i;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                System.arraycopy(MIN_INT_BYTES, 0, buf, pos, MIN_INT_BYTES.length);
                return pos + MIN_INT_BYTES.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }

        if (i < 1000) {
            int v = DIGITS_K_32[i];
            final int start = (byte) v;
            if (start == 0) {
                putShort(buf, pos, (short) (v >> 8));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 16);
            }
            buf[pos] = (byte) (v >> 24);
            return pos + 1;
        }

        final int q1 = i / 1000;
        final int r1 = i - q1 * 1000;
        final int v1 = DIGITS_K_32[r1];
        if (i < 1000000) {
            final int v2 = DIGITS_K_32[q1];
            int start = (byte) v2;
            if (start == 0) {
                putShort(buf, pos, (short) (v2 >> 8));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v2 >> 16);
            }
            putInt(buf, pos, v1 & 0xffffff00 | (v2 >> 24));
            return pos + 4;
        }
        final int q2 = q1 / 1000;
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final int v2 = DIGITS_K_32[r2];
        if (q3 == 0) {
            int v = DIGITS_K_32[q2];
            final int start = (byte) v;
            if (start == 0) {
                putShort(buf, pos, (short) (v >> 8));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (byte) (v >> 16);
            }
            buf[pos++] = (byte) (v >> 24);
        } else {
            putInt(buf, pos, DIGITS_K_32[q2 - q3 * 1000] & 0xffffff00 | (q3 + '0'));
            pos += 4;
        }

        putShort(buf, pos, (short) (v2 >> 8));
        putInt(buf, pos + 2, v1 & 0xffffff00 | (v2 >> 24));
        return pos + 6;
    }

    public static int writeInt32(final char[] buf, int pos, final int value) {
        int i;
        if (value < 0) {
            if (value == Integer.MIN_VALUE) {
                System.arraycopy(MIN_INT_CHARS, 0, buf, pos, MIN_INT_CHARS.length);
                return pos + MIN_INT_CHARS.length;
            }
            i = -value;
            buf[pos++] = '-';
        } else {
            i = value;
        }
        if (i < 1000) {
            long v = DIGITS_K_64[i];
            final int start = (byte) v;
            if (start == 0) {
                putInt(buf, pos, (int) (v >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v >> 32);
            }
            buf[pos] = (char) (v >> 48);
            return pos + 1;
        }
        final int q1 = i / 1000;
        final int r1 = i - q1 * 1000;
        final long v1 = DIGITS_K_64[r1];
        if (i < 1000000) {
            final long v2 = DIGITS_K_64[q1];
            int start = (byte) v2;
            if (start == 0) {
                putInt(buf, pos, (int) (v2 >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v2 >> 32);
            }
            putLong(buf, pos, v1 & 0xffffffffffff0000L | (v2 >> 48));
            return pos + 4;
        }
        final int q2 = q1 / 1000;
        final int r2 = q1 - q2 * 1000;
        final int q3 = q2 / 1000;
        final long v2 = DIGITS_K_64[r2];
        if (q3 == 0) {
            long v = DIGITS_K_64[q2];
            final int start = (byte) v;
            if (start == 0) {
                putInt(buf, pos, (int) (v >> 16));
                pos += 2;
            } else if (start == 1) {
                buf[pos++] = (char) (v >> 32);
            }
            buf[pos++] = (char) (v >> 48);
        } else {
            putLong(buf, pos, DIGITS_K_64[q2 - q3 * 1000]);
            buf[pos] = (char) (q3 + '0');
            pos += 4;
        }

        putInt(buf, pos, (int) (v2 >> 16));
        putLong(buf, pos + 2, v1 & 0xffffffffffff0000L | (v2 >> 48));
        return pos + 6;
    }

    public static void putChar(char[] buf, int pos, char v) {
        UNSAFE.putChar(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
    }

    public static void putShort(byte[] buf, int pos, short v) {
        UNSAFE.putShort(
                buf,
                ARRAY_BYTE_BASE_OFFSET + pos,
                BIG_ENDIAN ? Short.reverseBytes(v) : v
        );
    }

    public static void putInt(byte[] buf, int pos, int v) {
        UNSAFE.putInt(
                buf,
                ARRAY_BYTE_BASE_OFFSET + pos,
                BIG_ENDIAN ? Integer.reverseBytes(v) : v
        );
    }

    public static void putInt(char[] buf, int pos, int v) {
        UNSAFE.putInt(
                buf,
                ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1),
                BIG_ENDIAN ? Integer.reverseBytes(v) : v
        );
    }

    public static void putIntUnaligned(char[] buf, int pos, int v) {
        UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
    }

    public static void putLong(char[] buf, int pos, long v) {
        UNSAFE.putLong(
                buf,
                ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1),
                BIG_ENDIAN ? Long.reverseBytes(v) : v
        );
    }

    public static int digit4(char[] chars, int off) {
        long x = UNSAFE.getLong(chars, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1));
        if (BIG_ENDIAN) {
            x = Long.reverseBytes(x);
        }
        long d;
        if ((((x & 0xF000F000F000F0L) - 0x30003000300030L)
                | (((d = x & 0x0F000F000F000FL) + 0x06000600060006L) & 0xF000F000F000F0L)) != 0) {
            return -1;
        }
        return (int) (((d & 0xF) << 10) + ((d & 0xF) << 3) - ((d & 0xF) << 5) // (d & 0xF) * 1000
                + ((d & 0xF0000) >> 10) + ((d & 0xF0000) >> 11) + ((d & 0xF0000) >> 14) // ((d & 0xF0000) >> 16) * 100
                + ((d & 0xF00000000L) >> 29) + ((d & 0xF00000000L) >> 31) // ((d & 0xF00000000L) >> 32) * 10
                + (d >> 48));
    }

    public static int digit4(byte[] bytes, int off) {
        int x = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + off);
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        /*
            Here we are doing a 4-Byte Vector operation on the Int type.

            x & 0xF0 != 0xC0
            ---------------
            0 0b0011_0000 & 0b1111_0000 = 0b0011_0000
            1 0b0011_0001 & 0b1111_0000 = 0b0011_0000
            2 0b0011_0010 & 0b1111_0000 = 0b0011_0000
            3 0b0011_0011 & 0b1111_0000 = 0b0011_0000
            4 0b0011_0100 & 0b1111_0000 = 0b0011_0000
            5 0b0011_0101 & 0b1111_0000 = 0b0011_0000
            6 0b0011_0110 & 0b1111_0000 = 0b0011_0000
            7 0b0011_0111 & 0b1111_0000 = 0b0011_0000
            8 0b0011_1000 & 0b1111_0000 = 0b0011_0000
            9 0b0011_1001 & 0b1111_0000 = 0b0011_0000

            (((d = x & 0x0F) + 0x06) & 0xF0) != 0
            ---------------
            0 ((0b0011_0000) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            1 ((0b0011_0001) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            2 ((0b0011_0010) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            3 ((0b0011_0011) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            4 ((0b0011_0100) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            5 ((0b0011_0101) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            6 ((0b0011_0110) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            7 ((0b0011_0111) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            8 ((0b0011_1000) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
            9 ((0b0011_1001) & 0b0000_1111 + 0b0110_0000) & 0b1111_0000 = 0b0110_0000
         */
        int d;
        if ((((x & 0xF0F0F0F0) - 0x30303030) | (((d = x & 0x0F0F0F0F) + 0x06060606) & 0xF0F0F0F0)) != 0) {
            return -1;
        }
        /*
            10   = (1 << 3 ) + (1 << 1)
            100  = (1 << 6 ) + (1 << 5) + (1 << 2)
            1000 = (1 << 10) + (1 << 3) - (1 << 5)
         */
        return ((d & 0xF) << 10) + ((d & 0xF) << 3) - ((d & 0xF) << 5) // (d & 0xF) * 1000
                + ((d & 0xF00) >> 2) + ((d & 0xF00) >> 3) + ((d & 0xF00) >> 6) // ((d & 0xF00) >> 8) * 100
                + ((d & 0xF0000) >> 13) + ((d & 0xF0000) >> 15) // ((d & 0xF0000) >> 16) * 10
                + (d >> 24);
    }

    public static int digit3(char[] chars, int off) {
        long address = ARRAY_CHAR_BASE_OFFSET + ((long) off << 1);
        int i = UNSAFE.getInt(chars, address);
        short s = UNSAFE.getShort(chars, address + 4);
        if (BIG_ENDIAN) {
            i = Integer.reverseBytes(i);
            s = Short.reverseBytes(s);
        }
        long x = (((long) s) << 32) | i; // reuse
        long d;
        if ((((x & 0xF000F000F0L) - 0x3000300030L) | (((d = x & 0x0F000F000FL) + 0x0600060006L) & 0xF000F000F0L)) != 0) {
            return -1;
        }
        return (int) (((d & 0xF) << 6) + ((d & 0xF) << 5) + ((d & 0xF) << 2) // (d & 0xF) * 100
                + ((d & 0xF0000L) >> 13) + ((d & 0xF0000L) >> 15) // ((d & 0xF0000) >> 16) * 10
                + (d >> 32));
    }

    public static int digit3(byte[] bytes, int off) {
        int x = UNSAFE.getShort(bytes, ARRAY_BYTE_BASE_OFFSET + off);
        if (BIG_ENDIAN) {
            x = Short.reverseBytes((short) x);
        }
        x |= UNSAFE.getByte(bytes, ARRAY_BYTE_BASE_OFFSET + off + 2) << 16;
        int d;
        if ((((x & 0xF0F0F0) - 0x303030) | (((d = x & 0x0F0F0F) + 0x060606) & 0xF0F0F0)) != 0) {
            return -1;
        }
        return ((d & 0xF) << 6) + ((d & 0xF) << 5) + ((d & 0xF) << 2) // (d & 0xff) * 100
                + ((d & 0xF00) >> 5) + ((d & 0xF00) >> 7) // ((d & 0xF00) >> 8) * 10
                + (d >> 16);
    }

    public static int digit2(char[] chars, int off) {
        int x = UNSAFE.getInt(chars, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1));
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        int d;
        if ((((x & 0xF000F0) - 0x300030) | (((d = x & 0x0F000F) + 0x060006) & 0xF000F0)) != 0) {
            return -1;
        }
        return ((d & 0xF) << 3) + ((d & 0xF) << 1) // (d & 0xF) * 10
                + (d >> 16);
    }

    public static int digit2(byte[] bytes, int off) {
        short x = UNSAFE.getShort(bytes, ARRAY_BYTE_BASE_OFFSET + off);
        if (BIG_ENDIAN) {
            x = Short.reverseBytes(x);
        }
        int d;
        if ((((x & 0xF0F0) - 0x3030) | (((d = x & 0x0F0F) + 0x0606) & 0xF0F0)) != 0
        ) {
            return -1;
        }
        return ((d & 0xF) << 3) + ((d & 0xF) << 1)  // (d & 0xF) * 10
                + (d >> 8);
    }

    public static int digit1(char[] chars, int off) {
        int d = UNSAFE.getByte(chars, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1)) - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public static int digit1(byte[] bytes, int off) {
        int d = UNSAFE.getByte(bytes, ARRAY_BYTE_BASE_OFFSET + off) - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public static int indexOfChar(byte[] value, int ch, int fromIndex) {
        return indexOfChar(value, ch, fromIndex, value.length);
    }

    public static int indexOfChar(byte[] value, int ch, int fromIndex, int max) {
        if (INDEX_OF_CHAR_LATIN1 == null) {
            return indexOfChar0(value, ch, fromIndex, max);
        }
        try {
            return (int) INDEX_OF_CHAR_LATIN1.invokeExact(value, ch, fromIndex, max);
        } catch (Throwable e) {
            throw new JSONException(e.getMessage());
        }
    }

    private static int indexOfChar0(byte[] value, int ch, int fromIndex, int max) {
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

    public static int hexDigit4(byte[] bytes, int offset) {
        int v = Integer.reverseBytes(UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset));
        v = (v & 0x0F0F0F0F) + ((((v & 0x40404040) >> 2) | ((v & 0x40404040) << 1)) >>> 4);
        v = ((v >>> 12) & 0xF000)
                + ((v >>> 8) & 0xF00)
                + ((v >>> 4) & 0xF0)
                + (v & 0xF);
        return v;
    }

    public static boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    public static int getIntLittleEndian(byte[] bytes, int offset) {
        int v = UNSAFE.getInt(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
        if (BIG_ENDIAN) {
            v = Integer.reverseBytes(v);
        }
        return v;
    }

    public static long getLongBigEndian(byte[] bytes, int offset) {
        long v = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
        if (!BIG_ENDIAN) {
            v = Long.reverseBytes(v);
        }
        return v;
    }

    public static long getLongLittleEndian(byte[] bytes, int offset) {
        long v = UNSAFE.getLong(bytes, ARRAY_BYTE_BASE_OFFSET + offset);
        if (BIG_ENDIAN) {
            v = Long.reverseBytes(v);
        }
        return v;
    }
}
