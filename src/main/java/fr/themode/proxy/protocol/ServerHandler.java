package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;
import fr.themode.proxy.utils.ProtocolUtils;

import java.nio.ByteBuffer;

public class ServerHandler extends ProtocolHandler {
    @Override
    public void process(ConnectionContext connectionContext, int packetId, ByteBuffer payload) {
        //System.out.println("server " + Integer.toHexString(packetId) + " " + context.isCompression());

        if (packetId == 3) {
            // Compression packet
            final int threshold = ProtocolUtils.readVarInt(payload);
            connectionContext.setCompression(threshold);
            connectionContext.getTargetContext().setCompression(threshold);
        }
    }
}
