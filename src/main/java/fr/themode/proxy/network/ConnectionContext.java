package fr.themode.proxy.network;

import fr.themode.proxy.ConnectionState;
import fr.themode.proxy.protocol.Protocol;
import fr.themode.proxy.protocol.ProtocolHandler;
import fr.themode.proxy.utils.ProtocolUtils;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ConnectionContext {

    private final SocketChannel target;
    private final ProtocolHandler handler;
    private ConnectionState connectionState = ConnectionState.UNKNOWN;

    private boolean compression = false;
    private int compressionThreshold = 0;

    private ByteBuffer cacheBuffer;

    protected ConnectionContext targetConnectionContext;

    private final Protocol protocol = Protocol.VANILLA;

    public ConnectionContext(SocketChannel target, ProtocolHandler handler) {
        this.target = target;
        this.handler = handler;
    }

    public void processPackets(SocketChannel channel, WorkerContext workerContext) {
        ByteBuffer readBuffer = workerContext.readBuffer;
        final int limit = readBuffer.limit();
        // Read all packets
        while (readBuffer.remaining() > 0) {
            readBuffer.mark(); // Mark the beginning of the packet
            try {
                // Read packet
                final int packetLength = ProtocolUtils.readVarInt(readBuffer);
                final int packetEnd = readBuffer.position() + packetLength;
                if (packetEnd > readBuffer.limit()) {
                    // Integrity fail
                    throw new BufferUnderflowException();
                }

                readBuffer.limit(packetEnd);

                // Read protocol
                var content = workerContext.contentBuffer.clear();
                if (protocol.read(this, readBuffer, workerContext)) {
                    // Payload is available in the read buffer without any copy/transformation
                    content = readBuffer;
                } else {
                    // Make the content buffer readable
                    // Data has been copied over (probably involving decompression)
                    content.flip();
                }

                // Transform packet
                boolean transformed = false;
                var transformPayload = workerContext.transformPayload.clear();
                try {
                    transformed = this.handler.process(this, content, transformPayload);
                } catch (Exception e) {
                    // Error while reading the packet
                    e.printStackTrace();
                }

                // Write to cache/socket
                if (transformed) {
                    var target = workerContext.transform.clear();
                    this.protocol.write(this, transformPayload.flip(), target, workerContext);
                    transformPayload.clear();
                    if (!incrementalWrite(channel, target.flip(), workerContext)) {
                        break;
                    }
                } else {
                    // Packet hasn't been modified, write slice
                    readBuffer.reset(); // Return to the beginning of the packet
                    if (!incrementalWrite(channel, readBuffer, workerContext)) {
                        break;
                    }
                }

                // Check if the previous packet enabled compression
                {
                    if (!compression && compressionThreshold > 0) {
                        this.compression = true;
                    }
                }

                // Return to original state (before writing)
                readBuffer.limit(limit).position(packetEnd);
            } catch (BufferUnderflowException e) {
                readBuffer.reset();
                this.cacheBuffer = ByteBuffer.allocateDirect(readBuffer.remaining());
                this.cacheBuffer.put(readBuffer).flip();
                break;
            }
        }

        // Write remaining
        try {
            write(channel, workerContext.writeBuffer.flip());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void setCompression(int threshold) {
        this.compressionThreshold = threshold;
    }

    private boolean incrementalWrite(SocketChannel channel, ByteBuffer buffer, WorkerContext workerContext) {
        try {
            var writeBuffer = workerContext.writeBuffer;
            try {
                writeBuffer.put(buffer);
            } catch (BufferOverflowException e) {
                // Buffer is full, write in 2 steps
                write(channel, writeBuffer.flip());
                write(channel, buffer);
            }
            return true;
        } catch (IOException e) {
            // Connection probably closed
            return false;
        }
    }

    private void write(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.remaining() > 0) {
            channel.write(buffer);
        }
    }
}
