package fr.themode.proxy.protocol;

import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.utils.ConnectionStateUtils;

import java.nio.ByteBuffer;

public class ServerHandler extends ProtocolHandler {
    @Override
    public void process(ConnectionContext context, int packetId, ByteBuffer payload) {
        ConnectionStateUtils.handleServerState(context, packetId, payload);
    }
}
