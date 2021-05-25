package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;
import fr.themode.proxy.utils.ProtocolUtils;

import java.nio.ByteBuffer;

public class ClientHandler extends ProtocolHandler {
    @Override
    public void read(ConnectionContext connectionContext, ByteBuffer payload) {
        final int packetId = ProtocolUtils.readVarInt(payload);
        //System.out.println("client " + Integer.toHexString(packetId) + " " + context.isCompression());
    }
}
