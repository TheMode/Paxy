package fr.themode.proxy.protocol;

import fr.themode.proxy.Context;
import fr.themode.proxy.buffer.BufferUtils;

import java.nio.ByteBuffer;

public class ServerHandler extends ProtocolHandler {
    @Override
    public void read(Context context, ByteBuffer payload) {
        final int packetId = BufferUtils.readVarInt(payload);
        //System.out.println("server " + Integer.toHexString(packetId) + " " + context.isCompression());

        if (packetId == 3) {
            // Compression packet
            final int threshold = BufferUtils.readVarInt(payload);
            final boolean compression = threshold > 0;
            context.setCompression(compression, threshold);
            context.getTargetContext().setCompression(compression, threshold);
        }
    }
}
