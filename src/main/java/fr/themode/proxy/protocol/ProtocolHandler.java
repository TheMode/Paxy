package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;

import java.nio.ByteBuffer;

public abstract class ProtocolHandler {
    public abstract boolean process(ConnectionContext connectionContext, ByteBuffer payload, ByteBuffer transform);
}
