package fr.themode.proxy.protocol.packet;

import fr.themode.proxy.buffer.MinecraftBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Packet {

    private final Map<String, Field> fields = new HashMap<>();

    private boolean modified;

    public abstract void registerFields();

    public abstract void read(MinecraftBuffer in);

    public abstract void write(MinecraftBuffer out);

    public <T> void registerField(String name, Class<T> type, Supplier<T> getter, Consumer<T> setter) {
        this.fields.put(name, new Field(name, (Class<Object>) type,
                (Supplier<Object>) getter,
                o -> {
                    this.modified = true;
                    setter.accept((T) o);
                }));
    }

    public boolean isModified() {
        return modified;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public static class Field {
        public final String name;
        public final Class<Object> type;
        public final Supplier<Object> getter;
        public final Consumer<Object> setter;

        public Field(String name, Class<Object> type, Supplier<Object> getter, Consumer<Object> setter) {
            this.name = name;
            this.type = type;
            this.getter = getter;
            this.setter = setter;
        }
    }
}
