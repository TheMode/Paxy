package fr.themode.proxy;

import fr.themode.proxy.utils.ProtocolUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class MinecraftBuffer {

    private final ByteBuffer buffer;

    private MinecraftBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    public static MinecraftBuffer wrap(ByteBuffer buffer) {
        return new MinecraftBuffer(buffer);
    }

    public byte readByte() {
        return buffer.get();
    }

    public void writeByte(byte value) {
        this.buffer.put(value);
    }

    public int readVarInt() {
        return ProtocolUtils.readVarInt(buffer);
    }

    public void writeVarInt(int value) {
        ProtocolUtils.writeVarInt(buffer, value);
    }

    public String readString(int length) {
        return ProtocolUtils.readString(buffer, length);
    }

    public void writeString(String string) {
        ProtocolUtils.writeString(buffer, string);
    }

    public UUID readUuid() {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public void writeUuid(UUID uuid) {
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }
}
