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
        public void write(ConnectionContext context, ByteBuffer payload, ByteBuffer out, WorkerContext workerContext) {
            if (context.isCompression()) {
                final int decompressedSize = payload.remaining();
                final int lengthIndex = ProtocolUtils.writeEmptyVarIntHeader(out);
                final int contentStart = out.position();
                if (decompressedSize >= context.getCompressionThreshold()) {
                    ProtocolUtils.writeVarInt(out, decompressedSize);
                    CompressionUtils.compress(workerContext.deflater, payload, out);
                } else {
                    ProtocolUtils.writeVarInt(out, 0);
                    out.put(payload);
                }
                final int finalSize = out.position() - contentStart;
                ProtocolUtils.writeVarIntHeader(out, lengthIndex, finalSize);
            } else {
                // Length + payload
                ProtocolUtils.writeVarInt(out, payload.remaining());
                out.put(payload);
            }
        }
    };

    void read(ConnectionContext context, ByteBuffer buffer, WorkerContext workerContext);

    void write(ConnectionContext context, ByteBuffer payload, ByteBuffer out, WorkerContext workerContext);
}
