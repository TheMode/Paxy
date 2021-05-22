package fr.themode.proxy;

import fr.themode.proxy.buffer.Buffer;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Context {

    private final SocketChannel target;
    private final ConnectionType connectionType;

    private final Buffer contextBuffer = Buffer.buffer(ByteBuffer.allocate(20_000_000).limit(0));

    public Context(SocketChannel target, ConnectionType connectionType) {
        this.target = target;
        this.connectionType = connectionType;
    }

    public void processPackets(SocketChannel channel, ByteBuffer buffer, int readLength) {
        contextBuffer.append(buffer, readLength);
        System.out.println("process " + readLength + " " + contextBuffer.getByteBuffer() + " " + connectionType);

        ByteBuffer buf = contextBuffer.getByteBuffer();
        while (buf.remaining() > 0) {
            buf.mark();
            try {
                System.out.println("Start protocol read " + contextBuffer.getByteBuffer());
                final int length = contextBuffer.readVarInt();
                System.out.println("payload length: " + length + " buffer: " + contextBuffer.getByteBuffer());
                final byte[] data = contextBuffer.getBytes(length);

                readPayload(Buffer.reader(data));

                try {
                    final int end = contextBuffer.position();
                    buf.reset();

                    var b = buf.duplicate().limit(end).slice();
                    System.out.println("write " + b);
                    channel.write(b);

                    buf.position(end); // Continue...
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            } catch (BufferUnderflowException e) {
                //System.out.println("compact "+buf);
                this.contextBuffer.compact();
                //System.out.println("compact end "+buf);
                break;
            }
        }
    }

    private void readPayload(Buffer payload) {
        final int packetId = payload.readVarInt();
        System.out.println("Packet ID " + Integer.toHexString(packetId));
    }

    public SocketChannel getTarget() {
        return target;
    }

    private static String print(ByteBuffer byteBuffer) {
        byteBuffer.mark();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        byteBuffer.reset();
        return Arrays.toString(bytes);
    }
}
