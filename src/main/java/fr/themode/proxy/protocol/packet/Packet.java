package fr.themode.proxy.protocol.packet;

import fr.themode.proxy.buffer.MinecraftBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Packet {

    protected boolean modified;

    public abstract void read(MinecraftBuffer in);

    public abstract void write(MinecraftBuffer out);

    public abstract Map<String, Field> getFields();

    public boolean isModified() {
        return modified;
    }

    public static class FieldMap {
        private final Map<String, Field> fields = new HashMap<>();

        public <P extends Packet, T> void registerField(Class<P> packetType,
                                                        String name,
                                                        Class<T> type, Function<P, T> getter, BiConsumer<P, T> setter) {
            this.fields.put(name, new Field(name, (Class<Object>) type,
                    o -> getter.apply((P) o),
                    (packet, value) -> {
                        ((P) packet).modified = true;
                        setter.accept((P) packet, (T) value);
                    }));
        }

        public Map<String, Field> getFields() {
            return fields;
        }
    }

    public static class Field {
        public final String name;
        public final Class<Object> type;
        public final Function<Object, Object> getter;
        public final BiConsumer<Object, Object> setter;

        public Field(String name, Class<Object> type, Function<Object, Object> getter, BiConsumer<Object, Object> setter) {
            this.name = name;
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }
    }
}
