package fr.themode.proxy;

import fr.themode.proxy.buffer.BufferUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Context {

    private final SocketChannel target;
    private final ConnectionType connectionType;

    private final ByteBuffer contextBuffer = ByteBuffer.allocateDirect(Server.BUFFER).limit(0);

    public Context(SocketChannel target, ConnectionType connectionType) {
        this.target = target;
        this.connectionType = connectionType;
    }

    public void processPackets(SocketChannel channel, ByteBuffer buffer, int readLength) {
        BufferUtils.append(contextBuffer, buffer, readLength);
        //System.out.println("process " + readLength + " " + contextBuffer.getByteBuffer() + " " + connectionType);

        while (contextBuffer.remaining() > 0) {
            contextBuffer.mark();
            try {
                //System.out.println("Start protocol read " + contextBuffer.getByteBuffer());
                final int length = BufferUtils.readVarInt(contextBuffer);
                //System.out.println("payload length: " + length + " buffer: " + contextBuffer.getByteBuffer());
                final byte[] data = BufferUtils.getBytes(contextBuffer, length);

                readPayload(ByteBuffer.wrap(data));

                try {
                    final int end = contextBuffer.position();
                    contextBuffer.reset();

                    var slice = contextBuffer.duplicate().limit(end).slice();

                    // Block write
                    while (slice.position() != slice.limit()) {
                        channel.write(slice);
                    }

                    contextBuffer.position(end); // Continue...
                } catch (IOException e) {
                    // Connection probably closed
                    contextBuffer.reset();
                    break;
                }
            } catch (RuntimeException e) {
                // Probably a buffer underflow
                BufferUtils.compact(contextBuffer);
                break;
            }
        }
    }

    private void readPayload(ByteBuffer payload) {
        final int packetId = BufferUtils.readVarInt(payload);
        //System.out.println("Packet ID " + Integer.toHexString(packetId));
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
