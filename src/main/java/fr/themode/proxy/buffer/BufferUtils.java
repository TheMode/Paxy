package fr.themode.proxy.buffer;

import java.nio.ByteBuffer;

public class BufferUtils {

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
}
