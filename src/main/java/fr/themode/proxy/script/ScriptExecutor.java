package fr.themode.proxy.script;

import fr.themode.proxy.PacketBound;
import fr.themode.proxy.buffer.MinecraftBuffer;
import fr.themode.proxy.network.ConnectionContext;
import fr.themode.proxy.protocol.packet.Packet;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

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
        final var listeners = switch (bound) {
            case IN -> incomingListeners.get(name);
            case OUT -> outgoingListeners.get(name);
        };
        if (listeners == null || listeners.isEmpty()) {
            // Nothing to run
            return;
        }
        packet.ensureInitialization(in);
        final var polyglotPacket = new PolyglotPacket(packet);
        listeners.forEach(listener -> listener.accept(context, polyglotPacket));
    }

    public interface PacketListener extends BiConsumer<ConnectionContext, PolyglotPacket> {
    }

    static class PolyglotPacket implements ProxyObject {
        private final Packet packet;
        private final Map<String, Packet.Field> fields;

        PolyglotPacket(Packet packet) {
            this.packet = packet;
            this.fields = packet.getFields();
        }

        @Override
        public Object getMember(String key) {
            return fields.get(key).getter().apply(packet);
        }

        @Override
        public Object getMemberKeys() {
            return fields.keySet().toArray(String[]::new);
        }

        @Override
        public boolean hasMember(String key) {
            return fields.containsKey(key);
        }

        @Override
        public void putMember(String key, Value value) {
            final var field = fields.get(key);
            final var casted = value.as(field.type());
            field.setter().accept(packet, casted);
        }
    }

}
