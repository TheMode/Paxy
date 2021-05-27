package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;

import java.nio.ByteBuffer;

public abstract class ProtocolHandler {
    public abstract void process(ConnectionContext connectionContext, int packetId, ByteBuffer payload);
}
