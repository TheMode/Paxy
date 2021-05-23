package fr.themode.proxy;

import fr.themode.proxy.buffer.BufferUtils;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Context {

    private final SocketChannel target;
    private final ConnectionType connectionType;

    private ByteBuffer cacheBuffer;

    public Context(SocketChannel target, ConnectionType connectionType) {
        this.target = target;
        this.connectionType = connectionType;
    }

    public void processPackets(SocketChannel channel, ByteBuffer buffer) {
        //System.out.println("read "+buffer);
        //System.out.println("process " + readLength + " " + contextBuffer.getByteBuffer() + " " + connectionType);

        while (buffer.remaining() > 0) {
            buffer.mark();
            try {
                //System.out.println("Start protocol read " + contextBuffer.getByteBuffer());
                final int length = BufferUtils.readVarInt(buffer);
                //System.out.println("payload length: " + length + " buffer: " + contextBuffer.getByteBuffer());
                final byte[] data = BufferUtils.getBytes(buffer, length);

                readPayload(ByteBuffer.wrap(data));

                try {
                    final int end = buffer.position();
                    buffer.reset();

                    var slice = buffer.duplicate().limit(end).slice();

                    // Block write
                    while (slice.position() != slice.limit()) {
                        channel.write(slice);
                    }

                    buffer.position(end); // Continue...
                } catch (IOException e) {
                    // Connection probably closed
                    //System.out.println("error2");
                    buffer.reset();
                    break;
                }
            } catch (BufferUnderflowException e) {
                buffer.reset();
                this.cacheBuffer = ByteBuffer.allocateDirect(buffer.remaining());
                cacheBuffer.put(buffer).flip();
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

    public void consumeCache(ByteBuffer buffer) {
        if (cacheBuffer == null) {
            return;
        }
        buffer.put(cacheBuffer);
        this.cacheBuffer = null;
    }
}
