package fr.themode.proxy.script;

import fr.themode.proxy.MinecraftBuffer;
import fr.themode.proxy.PacketBound;
import fr.themode.proxy.State;
import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.packet.Packet;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ScriptExecutor {
    private final IntObjectHashMap<List<PacketListener>> outgoingListeners = new IntObjectHashMap<>();
    private final IntObjectHashMap<List<PacketListener>> incomingListeners = new IntObjectHashMap<>();

    public void registerOutgoing(String stateString, String packetName, PacketListener listener) {
        final State state = State.from(stateString);
        final int id = state.registry().getPacketId(PacketBound.OUT, packetName);
        this.outgoingListeners.getIfAbsentPut(id, ArrayList::new)
                .add(listener);
    }

    public void registerIncoming(String stateString, String packetName, PacketListener listener) {
        final State state = State.from(stateString);
        final int id = state.registry().getPacketId(PacketBound.IN, packetName);
        this.incomingListeners.getIfAbsentPut(id, ArrayList::new)
                .add(listener);
    }

    protected void run(ConnectionContext context, PacketBound bound, int id, Packet packet, MinecraftBuffer in) {
        final var listeners = switch (bound) {
            case IN -> incomingListeners.get(id);
            case OUT -> outgoingListeners.get(id);
        };
        if (listeners == null || listeners.isEmpty()) {
            // Nothing to run
            return;
        }
        packet.ensureInitialization(in);
        final var properties = context.getProperties();
        final var polyglotPacket = new PolyglotPacket(packet);
        listeners.forEach(listener -> listener.accept(properties, polyglotPacket));
    }

    public interface PacketListener extends BiConsumer<ProxyObject, PolyglotPacket> {
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
