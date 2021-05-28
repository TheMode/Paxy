package fr.themode.proxy.utils;

import java.nio.ByteBuffer;

public class ProtocolUtils {

    public static int readVarInt(ByteBuffer src) {
        // Code from https://github.com/bazelbuild/bazel/blob/master/src/main/java/com/google/devtools/build/lib/util/VarInt.java
        int tmp;
        if ((tmp = src.get()) >= 0) {
            return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = src.get()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = src.get()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = src.get()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = src.get()) << 28;
                    while (tmp < 0) {
                        // We get into this loop only in the case of overflow.
                        // By doing this, we can call getVarInt() instead of
                        // getVarLong() when we only need an int.
                        tmp = src.get();
                    }
                }
            }
        }
        return result;
    }

    public static void writeVarInt(ByteBuffer buffer, int value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buffer.put(temp);
        } while (value != 0);
    }

    public static int writeEmptyVarIntHeader(ByteBuffer buffer) {
        final int index = buffer.position();
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        buffer.put((byte) 0);
        return index;
    }

    public static void writeVarIntHeader(ByteBuffer buffer, int startIndex, int value) {
        final int indexCache = buffer.position();
        buffer.position(startIndex);
        final int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
        buffer.put((byte) ((w >> 16) & 0xFF));
        buffer.put((byte) ((w >> 8) & 0xFF));
        buffer.put((byte) (w & 0xFF));
        buffer.position(indexCache);
    }
}
