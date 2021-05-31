package fr.themode.proxy.transform;

import fr.themode.proxy.PacketBound;
import fr.themode.proxy.protocol.packet.Packet;
import fr.themode.proxy.protocol.packet.outgoing.play.OutChatMessagePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketRegistry {

    public static final PacketRegistry HANDSHAKE = handshake();
    public static final PacketRegistry STATUS = status();
    public static final PacketRegistry LOGIN = login();
    public static final PacketRegistry PLAY = play();

    // TODO avoid boxing
    private final Map<String, Integer> incomingByName = new HashMap<>();
    private final Map<Integer, String> incomingById = new HashMap<>();
    private final Map<Integer, Supplier<Packet>> incomingSupplierMap = new HashMap<>();

    private final Map<String, Integer> outgoingByName = new HashMap<>();
    private final Map<Integer, String> outgoingById = new HashMap<>();
    private final Map<Integer, Supplier<Packet>> outgoingSupplierMap = new HashMap<>();

    public String getPacketName(PacketBound bound, int id) {
        if (bound == PacketBound.IN) {
            return incomingById.get(id);
        } else if (bound == PacketBound.OUT) {
            return outgoingById.get(id);
        }
        return null;
    }

    public Packet getPacket(PacketBound bound, int id) {
        Supplier<Packet> supplier = null;
        if (bound == PacketBound.IN) {
            supplier = incomingSupplierMap.get(id);
        } else if (bound == PacketBound.OUT) {
            supplier = outgoingSupplierMap.get(id);
        }
        return supplier != null ? supplier.get() : null;
    }

    protected void registerIncoming(String name, int id, Supplier<Packet> supplier) {
        this.incomingByName.put(name, id);
        this.incomingById.put(id, name);
        this.incomingSupplierMap.put(id, supplier);
    }

    protected void registerOutgoing(String name, int id, Supplier<Packet> supplier) {
        this.outgoingByName.put(name, id);
        this.outgoingById.put(id, name);
        this.outgoingSupplierMap.put(id, supplier);
    }

    private static PacketRegistry handshake() {
        var registry = new PacketRegistry();
        registry.registerIncoming("handshake", 0x00, null);
        return registry;
    }

    private static PacketRegistry status() {
        var registry = new PacketRegistry();
        registry.registerIncoming("request", 0x00, null);
        registry.registerIncoming("ping", 0x01, null);

        registry.registerOutgoing("response", 0x00, null);
        registry.registerOutgoing("pong", 0x01, null);
        return registry;
    }

    private static PacketRegistry login() {
        var registry = new PacketRegistry();

        registry.registerIncoming("login-start", 0x00, null);
        registry.registerIncoming("encryption-response", 0x01, null);
        registry.registerIncoming("login-plugin-response", 0x02, null);

        registry.registerOutgoing("disconnect", 0x00, null);
        registry.registerOutgoing("encryption-request", 0x01, null);
        registry.registerOutgoing("login-success", 0x02, null);
        return registry;
    }

    private static PacketRegistry play() {
        var registry = new PacketRegistry();
        registry.registerOutgoing("chat-message", 0x0E, OutChatMessagePacket::new);
        return registry;
    }
}
