package fr.themode.proxy;

import fr.themode.proxy.buffer.BufferUtils;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Context {

    private final SocketChannel target;
    private final ConnectionType connectionType;
    private ConnectionState connectionState = ConnectionState.UNKNOWN;

    private ByteBuffer cacheBuffer;

    public Context(SocketChannel target, ConnectionType connectionType) {
        this.target = target;
        this.connectionType = connectionType;
    }

    public void processPackets(SocketChannel channel, ByteBuffer readBuffer, ByteBuffer writeBuffer) {
        // Read all packets
        while (readBuffer.remaining() > 0) {
            readBuffer.mark();
            try {
                // Read packet
                final int length = BufferUtils.readVarInt(readBuffer);
                final byte[] data = BufferUtils.getBytes(readBuffer, length);
                readPayload(ByteBuffer.wrap(data));

                // Write to cache or socket if full
                try {
                    final int end = readBuffer.position();
                    readBuffer.reset();

                    // Block write
                    var slice = readBuffer.duplicate().limit(end).slice();
                    try {
                        writeBuffer.put(slice);
                    } catch (BufferOverflowException e) {
                        write(channel, writeBuffer.flip());
                        write(channel, slice);
                    }

                    readBuffer.position(end); // Continue...
                } catch (IOException e) {
                    // Connection probably closed
                    readBuffer.reset();
                    break;
                }
            } catch (BufferUnderflowException e) {
                readBuffer.reset();
                this.cacheBuffer = ByteBuffer.allocateDirect(readBuffer.remaining());
                this.cacheBuffer.put(readBuffer).flip();
                break;
            }
        }

        // Write remaining
        if (writeBuffer.position() > 0) {
            try {
                write(channel, writeBuffer.flip());
            } catch (IOException e) {
                e.printStackTrace();
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

    private void write(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.position() != buffer.limit()) {
            channel.write(buffer);
        }
    }
}
