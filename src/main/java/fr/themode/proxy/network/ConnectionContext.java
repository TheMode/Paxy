package fr.themode.proxy.network;

import fr.themode.proxy.ConnectionState;
import fr.themode.proxy.protocol.ProtocolHandler;
import fr.themode.proxy.utils.CompressionUtils;
import fr.themode.proxy.utils.ProtocolUtils;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.zip.DataFormatException;

public class ConnectionContext {

    private final SocketChannel target;
    private final ProtocolHandler handler;
    private ConnectionState connectionState = ConnectionState.UNKNOWN;

    private boolean compression = false;
    private int compressionThreshold = 0;

    private ByteBuffer cacheBuffer;

    protected ConnectionContext targetConnectionContext;

    public ConnectionContext(SocketChannel target, ProtocolHandler handler) {
        this.target = target;
        this.handler = handler;
    }

    public void processPackets(SocketChannel channel, WorkerContext workerContext) {
        ByteBuffer readBuffer = workerContext.readBuffer;
        ByteBuffer writeBuffer = workerContext.writeBuffer;
        // Read all packets
        while (readBuffer.remaining() > 0) {
            readBuffer.mark();
            try {
                // Read packet
                final int packetLength = ProtocolUtils.readVarInt(readBuffer);

                try {
                    // Retrieve payload buffer
                    ByteBuffer payload = readBuffer.slice().limit(packetLength);
                    processPacket(payload, packetLength, workerContext);
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

    private void processPacket(ByteBuffer buffer, int length, WorkerContext workerContext) throws BufferUnderflowException {
        var contentBuffer = workerContext.contentBuffer;
        if (compression) {
            int position = buffer.position();
            final int dataLength = ProtocolUtils.readVarInt(buffer);
            if (dataLength == 0) {
                // Uncompressed
                final int size = buffer.position() - position;
                contentBuffer.clear().limit(length - size).put(buffer).flip();
            } else {
                // Compressed
                try {
                    var compressed = buffer.slice();
                    CompressionUtils.decompress(workerContext.inflater, compressed, dataLength, contentBuffer);
                } catch (DataFormatException e) {
                    e.printStackTrace();
                }
            }
        } else {
            contentBuffer.clear().limit(length).put(buffer).flip();
        }
        this.handler.read(this, contentBuffer);
    }

    public SocketChannel getTarget() {
        return target;
    }

    public ConnectionContext getTargetContext() {
        return targetConnectionContext;
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
