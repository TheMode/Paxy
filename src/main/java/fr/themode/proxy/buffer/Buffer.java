package fr.themode.proxy.buffer;

import java.nio.ByteBuffer;

public class Buffer {

    private final ByteBuffer byteBuffer;

    private Buffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public static Buffer buffer(ByteBuffer byteBuffer) {
        return new Buffer(byteBuffer);
    }

    public static Buffer reader(byte[] data) {
        return new Buffer(ByteBuffer.wrap(data));
    }

    public byte[] getBytes(int length) {
        byte[] data = new byte[length];
        byteBuffer.get(data);
        return data;
    }

    public int readVarInt() {
        int i = 0;
        final int maxRead = Math.min(5, byteBuffer.remaining());
        for (int j = 0; j < maxRead; j++) {
            final int k = byteBuffer.get();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }
        throw new RuntimeException("VarInt is too big");
    }

    public void append(ByteBuffer buffer, int length) {
        // Append to buffer, keep same position
        int positionCache = byteBuffer.position();
        int limitCache = byteBuffer.limit();
        //System.out.println("append start " + byteBuffer + " " + length);
        this.byteBuffer.position(limitCache).limit(limitCache + length);
        //System.out.println("limit "+byteBuffer.remaining()+" "+byteBuffer.limit());
        this.byteBuffer.put(buffer).position(positionCache);
        //System.out.println("append end " + byteBuffer);
    }

    public void compact() {
        // Create a new buffer with indexes [0;remaining]
        //System.out.println("prev buffer " + byteBuffer);
        byteBuffer.reset();
        byteBuffer.compact();
        byteBuffer.limit(byteBuffer.position()).position(0);
        //System.out.println("reduced " + byteBuffer);
    }

    public int position() {
        return byteBuffer.position();
    }

    public int readable() {
        return byteBuffer.remaining();
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }
}
