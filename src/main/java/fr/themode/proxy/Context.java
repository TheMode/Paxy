package fr.themode.proxy;

import fr.themode.proxy.utils.ProtocolUtils;
import fr.themode.proxy.protocol.ProtocolHandler;
import fr.themode.proxy.utils.CompressionUtils;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.zip.DataFormatException;

public class Context {

    private final SocketChannel target;
    private final ProtocolHandler handler;
    private ConnectionState connectionState = ConnectionState.UNKNOWN;

    private boolean compression = false;
    private int compressionThreshold = 0;

    private ByteBuffer cacheBuffer;

    protected Context targetContext;

    public Context(SocketChannel target, ProtocolHandler handler) {
        this.target = target;
        this.handler = handler;
    }

    public void processPackets(SocketChannel channel, ByteBuffer readBuffer, ByteBuffer writeBuffer, ByteBuffer contentBuffer) {
        // Read all packets
        while (readBuffer.remaining() > 0) {
            readBuffer.mark();
            try {
                // Read packet
                final int packetLength = ProtocolUtils.readVarInt(readBuffer);

                try {
                    // Retrieve payload buffer
                    ByteBuffer payload = readBuffer.slice().limit(packetLength);
                    processPacket(payload, packetLength, contentBuffer);
                } catch (IllegalArgumentException e) {
                    // Incomplete packet
                    throw new BufferUnderflowException();
                }

                try {
                    readBuffer.position(readBuffer.position() + packetLength); // Skip payload
                } catch (IllegalArgumentException e) {
                    // Incomplete packet
                    throw new BufferUnderflowException();
                }

                // Write to cache or socket if full
                try {
                    final int end = readBuffer.position();
                    readBuffer.reset();

                    // Block write
                    final int size = end - readBuffer.position();
                    var slice = readBuffer.slice().limit(size);
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

    private void processPacket(ByteBuffer buffer, int length, ByteBuffer contentBuffer) throws BufferUnderflowException {
        if (compression) {
            int position = buffer.position();
            final int dataLength = ProtocolUtils.readVarInt(buffer);

            if (dataLength == 0) {
                // Uncompressed
                int size = buffer.position() - position;

                var content = contentBuffer.slice().limit(length - size);
                content.put(buffer).flip();
                handler.read(this, content);
            } else {
                // Compressed
                try {
                    var compressed = buffer.slice();
                    CompressionUtils.decompress(compressed, dataLength, contentBuffer);
                    handler.read(this, contentBuffer);
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            }
        } else {
            var content = contentBuffer.slice().limit(length);
            content.put(buffer).flip();
            handler.read(this, content);
        }
    }

    public SocketChannel getTarget() {
        return target;
    }

    public Context getTargetContext() {
        return targetContext;
    }

    public void consumeCache(ByteBuffer buffer) {
        if (cacheBuffer == null) {
            return;
        }
        buffer.put(cacheBuffer);
        this.cacheBuffer = null;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public boolean isCompression() {
        return compression;
    }

    public int getCompressionThreshold() {
        return compressionThreshold;
    }

    public void setCompression(boolean compression, int threshold) {
        this.compression = compression;
        this.compressionThreshold = threshold;
    }

    private void write(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.position() != buffer.limit()) {
            channel.write(buffer);
        }
    }
}
