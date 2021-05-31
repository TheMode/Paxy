package fr.themode.proxy.script;

import fr.themode.proxy.protocol.packet.Packet;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import java.util.Map;

public class PolyglotPacket implements ProxyObject {

    private final Map<String, Packet.Field> fields;

    public PolyglotPacket(Packet packet) {
        this.fields = packet.getFields();
    }

    @Override
    public Object getMember(String key) {
        return fields.get(key).getter.get();
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
        final var casted = value.as(field.type);
        field.setter.accept(casted);
    }
}
