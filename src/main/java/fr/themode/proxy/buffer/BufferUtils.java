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

    public static byte[] getBytes(ByteBuffer byteBuffer, int length) {
        byte[] data = new byte[length];
        byteBuffer.get(data);
        return data;
    }

    public static void append(ByteBuffer byteBuffer, ByteBuffer buffer, int length) {
        // Append to buffer, keep same position
        final int positionCache = byteBuffer.position();
        final int limitCache = byteBuffer.limit();
        //System.out.println("append start " + byteBuffer + " " + length);
        byteBuffer.position(limitCache).limit(limitCache + length);
        //System.out.println("limit "+byteBuffer.remaining()+" "+byteBuffer.limit());
        byteBuffer.put(buffer).position(positionCache);
        //System.out.println("append end " + byteBuffer);
    }

    public static void compact(ByteBuffer byteBuffer) {
        // Create a new buffer with indexes [0;remaining]
        byteBuffer.reset().compact().flip();
    }
}
