package fr.themode.proxy.protocol.packet.outgoing.play;

import fr.themode.proxy.MinecraftBuffer;
import fr.themode.proxy.protocol.packet.Packet;

import java.util.Map;
import java.util.UUID;

public class OutChatMessagePacket extends Packet {

    private static final FieldMap FIELD_MAP = new FieldMap();

    public String message;
    public byte position;
    public UUID sender;

    static {
        FIELD_MAP.registerField(OutChatMessagePacket.class, "message", String.class,
                (packet) -> packet.message, (packet, value) -> packet.message = value);
        FIELD_MAP.registerField(OutChatMessagePacket.class, "position", byte.class,
                (packet) -> packet.position, (packet, value) -> packet.position = value);
        FIELD_MAP.registerField(OutChatMessagePacket.class, "sender", UUID.class,
                (packet) -> packet.sender, (packet, value) -> packet.sender = value);
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

    @Override
    public Map<String, Field> getFields() {
        return FIELD_MAP.getFields();
    }
}
