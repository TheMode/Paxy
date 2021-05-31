package fr.themode.proxy.protocol.packet.outgoing.play;

import fr.themode.proxy.protocol.packet.Packet;
import fr.themode.proxy.utils.ProtocolUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class OutgoingChatMessagePacket extends Packet {

    public String message;
    public byte position;
    public UUID sender;

    @Override
    public void registerFields() {
        registerField("message", String.class, () -> message, s -> message = s);
        registerField("position", byte.class, () -> position, s -> position = s);
        registerField("sender", UUID.class, () -> sender, s -> sender = s);
    }

    @Override
    public void read(ByteBuffer in) {
        this.message = ProtocolUtils.readString(in, 262144);
        this.position = in.get();
        this.sender = new UUID(in.getLong(), in.getLong());
    }

    @Override
    public void write(ByteBuffer out) {
        ProtocolUtils.writeVarInt(out, 0x0E);
        ProtocolUtils.writeString(out, message);
        out.put(position);

        out.putLong(sender.getMostSignificantBits());
        out.putLong(sender.getLeastSignificantBits());
    }
}
