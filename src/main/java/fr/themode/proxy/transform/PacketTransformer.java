package fr.themode.proxy.transform;

import fr.themode.proxy.network.ConnectionContext;

import java.nio.ByteBuffer;

public interface PacketTransformer {
    void transform(ConnectionContext context, ByteBuffer in, ByteBuffer out);
}
