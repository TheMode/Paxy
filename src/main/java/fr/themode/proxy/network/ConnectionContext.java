package fr.themode.proxy.network;

import fr.themode.proxy.ConnectionState;
import fr.themode.proxy.protocol.Protocol;
import fr.themode.proxy.protocol.ProtocolHandler;
import fr.themode.proxy.transform.PacketTransformer;
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

    private final TransformResult transformResult = new TransformResult();

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
                if (protocol.read(this, readBuffer, content, workerContext)) {
                    // Payload is available in the read buffer without any copy/transformation
                    content = readBuffer;
                } else {
                    // Make the content buffer readable
                    // Data has been copied over (probably involving decompression)
                    content.flip();
                }

                // Apply packet transformation
                transform(this, content, workerContext.transformPayload.clear(), transformResult);
                content = transformResult.buffer;

                final int contentPositionCache = content.position();

                // Write to cache/socket
                ByteBuffer writeCache;
                if (transformResult.transformed) {
                    writeCache = workerContext.transform.clear();
                    this.protocol.write(this, content, writeCache, workerContext);
                    writeCache.flip();
                } else {
                    // Packet hasn't been modified, write slice
                    writeCache = readBuffer.reset(); // to the beginning of the packet
                }
                if (!incrementalWrite(channel, writeCache, workerContext)) {
                    break;
                }

                content.position(contentPositionCache);

                // Process packet
                final int packetId = ProtocolUtils.readVarInt(content);
                try {
                    this.handler.process(this, packetId, content);
                } catch (Exception e) {
                    // Error while reading the packet
                    e.printStackTrace();
                    break;
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
        this.compression = threshold > 0;
        this.compressionThreshold = threshold;
    }

    private void transform(ConnectionContext context, ByteBuffer in, ByteBuffer out, TransformResult transformResult) {
        PacketTransformer transformer = null; // TODO transform API
        if (transformer == null) {
            transformResult.buffer = in;
            transformResult.transformed = false;
            return;
        }
        transformer.transform(context, in, out);
        transformResult.buffer = out.flip();
        transformResult.transformed = true;
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

    private static class TransformResult {
        ByteBuffer buffer;
        boolean transformed;
    }
}
