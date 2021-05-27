package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;

import java.nio.ByteBuffer;

public class ClientHandler extends ProtocolHandler {
    @Override
    public void process(ConnectionContext connectionContext, int packetId, ByteBuffer payload) {
        //System.out.println("client " + Integer.toHexString(packetId) + " " + context.isCompression());
    }
}
