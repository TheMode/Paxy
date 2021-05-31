package fr.themode.proxy;

import fr.themode.proxy.transform.PacketRegistry;

public enum ConnectionState {
    HANDSHAKE(PacketRegistry.HANDSHAKE),
    STATUS(PacketRegistry.STATUS),
    LOGIN(PacketRegistry.LOGIN),
    PLAY(PacketRegistry.PLAY);

    private final PacketRegistry registry;

    ConnectionState(PacketRegistry registry) {
        this.registry = registry;
    }

    public PacketRegistry getRegistry() {
        return registry;
    }
}
