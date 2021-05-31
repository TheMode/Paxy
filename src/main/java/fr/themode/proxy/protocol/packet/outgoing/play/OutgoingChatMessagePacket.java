package fr.themode.proxy.protocol.packet.outgoing.play;

import fr.themode.proxy.buffer.MinecraftBuffer;
import fr.themode.proxy.protocol.packet.Packet;

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
    public void read(MinecraftBuffer in) {
        this.message = in.readString(262144);
        this.position = in.readByte();
        this.sender = in.readUuid();
    }

    @Override
    public void write(MinecraftBuffer out) {
        out.writeString(message);
        out.writeByte(position);
        out.writeUuid(sender);
    }
}
