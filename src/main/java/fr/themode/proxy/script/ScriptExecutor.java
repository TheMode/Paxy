package fr.themode.proxy.script;

import fr.themode.proxy.PacketBound;
import fr.themode.proxy.buffer.MinecraftBuffer;
import fr.themode.proxy.network.ConnectionContext;
import fr.themode.proxy.protocol.packet.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ScriptExecutor {

    private final Map<String, List<PacketListener>> outgoingListeners = new HashMap<>();
    private final Map<String, List<PacketListener>> incomingListeners = new HashMap<>();

    public void registerOutgoing(String packetName, PacketListener listener) {
        this.outgoingListeners.computeIfAbsent(packetName, s -> new ArrayList<>())
                .add(listener);
    }

    public void registerIncoming(String packetName, PacketListener listener) {
        this.incomingListeners.computeIfAbsent(packetName, s -> new ArrayList<>())
                .add(listener);
    }

    protected void run(ConnectionContext context, PacketBound bound, String name, Packet packet, MinecraftBuffer in) {
        List<PacketListener> listeners = bound == PacketBound.OUT ? outgoingListeners.get(name) : incomingListeners.get(name);
        if (listeners == null || listeners.isEmpty()) {
            // Nothing to run
            return;
        }
        packet.ensureInitialization(in);
        PolyglotPacket polyglotPacket = new PolyglotPacket(packet);
        listeners.forEach(listener -> listener.accept(context, polyglotPacket));
    }

    private interface PacketListener extends BiConsumer<ConnectionContext, PolyglotPacket> {
    }
}
