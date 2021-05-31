package fr.themode.proxy.transform;

import fr.themode.proxy.PacketBound;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    public static final PacketRegistry HANDSHAKE = handshake();
    public static final PacketRegistry STATUS = status();
    public static final PacketRegistry LOGIN = login();
    public static final PacketRegistry PLAY = play();

    // TODO avoid boxing
    private final Map<String, Integer> incomingByName = new HashMap<>();
    private final Map<Integer, String> incomingById = new HashMap<>();

    private final Map<String, Integer> outgoingByName = new HashMap<>();
    private final Map<Integer, String> outgoingById = new HashMap<>();

    public String getPacketName(PacketBound bound, int id) {
        if (bound == PacketBound.INBOUND) {
            return incomingById.get(id);
        } else if (bound == PacketBound.OUTBOUND) {
            return outgoingById.get(id);
        }
        return null;
    }

    protected void registerIncoming(String name, int id) {
        this.incomingByName.put(name, id);
        this.incomingById.put(id, name);
    }

    protected void registerOutgoing(String name, int id) {
        this.outgoingByName.put(name, id);
        this.outgoingById.put(id, name);
    }

    private static PacketRegistry handshake() {
        var registry = new PacketRegistry();
        registry.registerIncoming("handshake", 0x00);
        return registry;
    }

    private static PacketRegistry status() {
        var registry = new PacketRegistry();
        registry.registerIncoming("request", 0x00);
        registry.registerIncoming("ping", 0x01);

        registry.registerOutgoing("response", 0x00);
        registry.registerOutgoing("pong", 0x01);
        return registry;
    }

    private static PacketRegistry login() {
        var registry = new PacketRegistry();

        registry.registerIncoming("login-start", 0x00);
        registry.registerIncoming("encryption-response", 0x01);
        registry.registerIncoming("login-plugin-response", 0x02);

        registry.registerOutgoing("disconnect", 0x00);
        registry.registerOutgoing("encryption-request", 0x01);
        registry.registerOutgoing("login-success", 0x02);
        return registry;
    }

    private static PacketRegistry play() {
        var registry = new PacketRegistry();
        registry.registerOutgoing("chat-message", 0x0E);
        return registry;
    }
}
