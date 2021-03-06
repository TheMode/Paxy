package fr.themode.proxy.packet;

import fr.themode.proxy.MinecraftBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Packet {

    protected boolean initialized, modified;

    public abstract void read(MinecraftBuffer in);

    public abstract void write(MinecraftBuffer out);

    public abstract Map<String, Field> getFields();

    public void ensureInitialization(MinecraftBuffer in) {
        if (!initialized) {
            read(in);
            this.initialized = true;
        }
    }

    public boolean isModified() {
        return modified;
    }

    public static final class FieldMap<P extends Packet> {
        private final Map<String, Field> fields = new HashMap<>();

        @SafeVarargs
        public FieldMap(Class<P> packetType, FieldEntry<P, ?>... entries) {
            for (var entry : entries) {
                this.fields.put(entry.name, entry.createField());
            }
        }

        public Map<String, Field> getFields() {
            return fields;
        }
    }

    public static record FieldEntry<P extends Packet, T>(String name, Class<T> type,
                                                         Function<P, T> getter, BiConsumer<P, T> setter) {
        public Field createField() {
            return new Field(name, (Class<Object>) type,
                    o -> getter.apply((P) o),
                    (packet, value) -> {
                        ((P) packet).modified = true;
                        setter.accept((P) packet, (T) value);
                    });
        }
    }

    public record Field(String name, Class<Object> type,
                        Function<Object, Object> getter,
                        BiConsumer<Object, Object> setter) {
    }
}
