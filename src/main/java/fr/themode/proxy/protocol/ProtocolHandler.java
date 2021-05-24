package fr.themode.proxy.protocol;

import fr.themode.proxy.Context;

import java.nio.ByteBuffer;

public abstract class ProtocolHandler {
    public abstract void read(Context context, ByteBuffer payload);
}
