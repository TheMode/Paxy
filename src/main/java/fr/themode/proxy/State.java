package fr.themode.proxy;

import fr.themode.proxy.packet.PacketRegistry;

import java.util.Locale;

/**
 * Represents a connection state.
 */
public enum State {
    HANDSHAKE(PacketRegistry.HANDSHAKE),
    STATUS(PacketRegistry.STATUS),
    LOGIN(PacketRegistry.LOGIN),
    PLAY(PacketRegistry.PLAY);

    private final PacketRegistry registry;

    State(PacketRegistry registry) {
        this.registry = registry;
    }

    public PacketRegistry registry() {
        return registry;
    }

    public static State from(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
