package fr.themode.proxy.utils;

import java.nio.ByteBuffer;

public class ProtocolUtils {

    public static int readVarInt(ByteBuffer byteBuffer, int maxRead) {
        int i = 0;
        for (int j = 0; j < maxRead; j++) {
            final int k = byteBuffer.get();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }
        throw new RuntimeException("VarInt is too big");
    }

    public static int readVarInt(ByteBuffer byteBuffer) {
        return readVarInt(byteBuffer, 3);
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
