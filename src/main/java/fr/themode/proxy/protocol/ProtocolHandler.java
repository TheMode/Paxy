package fr.themode.proxy.protocol;

import fr.themode.proxy.network.ConnectionContext;

import java.nio.ByteBuffer;

public abstract class ProtocolHandler {
    public abstract void read(ConnectionContext connectionContext, ByteBuffer payload);
}
