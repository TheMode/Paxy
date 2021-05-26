package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;
import fr.themode.proxy.network.WorkerContext;
import fr.themode.proxy.utils.CompressionUtils;
import fr.themode.proxy.utils.ProtocolUtils;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

public interface Protocol {

    Protocol VANILLA = new Protocol() {
        @Override
        public void read(ConnectionContext context, ByteBuffer buffer, WorkerContext workerContext) {
            final var compression = context.isCompression();
            var contentBuffer = workerContext.contentBuffer;
            if (compression) {
                final int dataLength = ProtocolUtils.readVarInt(buffer);
                if (dataLength == 0) {
                    // Uncompressed
                    contentBuffer.put(buffer);
                } else {
                    // Compressed
                    try {
                        CompressionUtils.decompress(workerContext.inflater, buffer, contentBuffer);
                    } catch (DataFormatException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // Compression disabled
                contentBuffer.put(buffer);
            }
        }

        @Override
        public void write(ConnectionContext context, ByteBuffer buffer, WorkerContext workerContext) {
            final boolean compression = context.isCompression();

            var payload = workerContext.transformPayload;
            var target = workerContext.transform;
            if (compression) {
                final int decompressedSize = payload.remaining();

                final int lengthIndex = ProtocolUtils.writeEmptyVarIntHeader(target);
                final int contentStart = target.position();

                final int threshold = context.getCompressionThreshold();
                if (decompressedSize >= threshold) {
                    ProtocolUtils.writeVarInt(target, decompressedSize);
                    CompressionUtils.compress(workerContext.deflater, payload, target);
                } else {
                    ProtocolUtils.writeVarInt(target, 0);
                    target.put(payload);
                }
                final int finalSize = target.position() - contentStart;
                ProtocolUtils.writeVarIntHeader(target, lengthIndex, finalSize);
            } else {
                ProtocolUtils.writeVarInt(target, payload.remaining());
                target.put(payload);
            }
        }
    };

    void read(ConnectionContext context, ByteBuffer buffer, WorkerContext workerContext);

    void write(ConnectionContext context, ByteBuffer buffer, WorkerContext workerContext);
}
