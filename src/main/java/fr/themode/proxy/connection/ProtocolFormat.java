package fr.themode.proxy.connection;

import fr.themode.proxy.utils.CompressionUtils;
import fr.themode.proxy.utils.ProtocolUtils;
import fr.themode.proxy.worker.WorkerContext;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

/**
 * Parses protocol data being forwarded to the transformer.
 */
public interface ProtocolFormat {

    /**
     * Packet format as described at https://wiki.vg/Protocol#Packet_format
     */
    ProtocolFormat VANILLA = new ProtocolFormat() {
        @Override
        public boolean read(ConnectionContext context, ByteBuffer buffer, ByteBuffer payloadOut, WorkerContext workerContext) {
            if (!context.isCompression()) {
                // Compression disabled, payload is following
                return true;
            }
            final int dataLength = ProtocolUtils.readVarInt(buffer);
            if (dataLength == 0) {
                // Data is too small to be compressed, payload is following
                return true;
            }
            // Decompress to content buffer
            try {
                CompressionUtils.decompress(workerContext.inflater, buffer, payloadOut);
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void write(ConnectionContext context, ByteBuffer payload, ByteBuffer out, WorkerContext workerContext) {
            if (!context.isCompression()) {
                // Length + payload
                ProtocolUtils.writeVarInt(out, payload.remaining());
                out.put(payload);
                return;
            }
            // Compressed format
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
        }
    };

    boolean read(ConnectionContext context, ByteBuffer buffer, ByteBuffer payloadOut, WorkerContext workerContext);

    void write(ConnectionContext context, ByteBuffer payload, ByteBuffer out, WorkerContext workerContext);
}
