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
        public boolean read(ConnectionContext context, ByteBuffer buffer, WorkerContext workerContext) {
            if (!context.isCompression()) {
                // Compression disabled, payload is following
                return true;
            }
            final int dataLength = ProtocolUtils.readVarInt(buffer);
            if (dataLength == 0) {
                // Data is too small to be compressed, payload is following
                return true;
            }

            // Compressed
            try {
                CompressionUtils.decompress(workerContext.inflater, buffer, workerContext.contentBuffer);
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
            return false;
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

    boolean read(ConnectionContext context, ByteBuffer buffer, WorkerContext workerContext);

    void write(ConnectionContext context, ByteBuffer payload, ByteBuffer out, WorkerContext workerContext);
}
