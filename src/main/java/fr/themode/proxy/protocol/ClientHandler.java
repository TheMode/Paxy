package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;
import fr.themode.proxy.utils.ConnectionStateUtils;

import java.nio.ByteBuffer;

public class ClientHandler extends ProtocolHandler {
    @Override
    public void process(ConnectionContext context, int packetId, ByteBuffer payload) {
        ConnectionStateUtils.handleClientState(context, packetId, payload);
    }
}
