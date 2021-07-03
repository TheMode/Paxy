package fr.themode.proxy.packet;

import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.script.ScriptLocal;
import fr.themode.proxy.utils.ProtocolUtils;

import java.nio.ByteBuffer;

public interface PacketTransformer {
    PacketTransformer VANILLA = (context, in, out, scriptLocal) -> {
        final int packetId = ProtocolUtils.readVarInt(in);
        return scriptLocal.run(context, packetId, in, out);
    };

    boolean transform(ConnectionContext context, ByteBuffer in, ByteBuffer out, ScriptLocal scriptLocal);
}
