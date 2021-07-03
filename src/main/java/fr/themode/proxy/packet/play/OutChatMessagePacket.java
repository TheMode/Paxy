package fr.themode.proxy.packet.play;

import fr.themode.proxy.MinecraftBuffer;
import fr.themode.proxy.packet.Packet;

import java.util.Map;
import java.util.UUID;

public class OutChatMessagePacket extends Packet {
    private static final FieldMap<OutChatMessagePacket> FIELD_MAP = new FieldMap<>(OutChatMessagePacket.class,
            new FieldEntry<>("message", String.class, (packet) -> packet.message, (packet, value) -> packet.message = value),
            new FieldEntry<>("position", byte.class, (packet) -> packet.position, (packet, value) -> packet.position = value),
            new FieldEntry<>("sender", UUID.class, (packet) -> packet.sender, (packet, value) -> packet.sender = value));

    private String message;
    private byte position;
    private UUID sender;

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
