package fr.themode.proxy.protocol;

import fr.themode.proxy.connection.ConnectionContext;

import java.nio.ByteBuffer;

public abstract class ProtocolHandler {
    public abstract void process(ConnectionContext context, int packetId, ByteBuffer payload);
}
