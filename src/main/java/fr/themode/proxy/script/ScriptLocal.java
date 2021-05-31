package fr.themode.proxy.script;

import fr.themode.proxy.buffer.MinecraftBuffer;
import fr.themode.proxy.network.ConnectionContext;
import fr.themode.proxy.protocol.packet.Packet;
import fr.themode.proxy.protocol.packet.outgoing.play.OutgoingChatMessagePacket;
import fr.themode.proxy.utils.ProtocolUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ScriptLocal {

    private final List<Script> scripts = new ArrayList<>();

    private ScriptLocal(String folder) {
        loadFolder(folder);
    }

    public static ScriptLocal load(String folder) {
        return new ScriptLocal(folder);
    }

    public boolean run(ConnectionContext context, int packetId, ByteBuffer in, ByteBuffer out) {
        final var bound = context.getPacketBound();
        final var registry = context.getState().getRegistry();
        final String packetName = registry.getPacketName(bound, packetId);
        if (packetName == null) {
            // Unknown packet, see registry
            return false;
        }
        // TODO dont hardcode
        if (packetId != 0x0e)
            return false;

        Packet packet = new OutgoingChatMessagePacket();
        packet.registerFields();
        packet.read(MinecraftBuffer.wrap(in));
        scripts.forEach(script -> script.getExecutor().run(context, bound, packetName, packet));
        if (packet.isModified()) {
            ProtocolUtils.writeVarInt(out, packetId);
            packet.write(MinecraftBuffer.wrap(out));
            return true;
        }
        return false;
    }

    private void loadFolder(String folder) {
        final File scriptFolder = new File(folder);
        if (!scriptFolder.exists()) {
            return; // No script folder
        }
        final File[] folderFiles = scriptFolder.listFiles();
        if (folderFiles == null) {
            System.err.println(scriptFolder + " is not a folder!");
            return;
        }

        for (File file : folderFiles) {
            final String name = file.getName();
            if (file.isDirectory()) {
                continue;
            }
            Script script = new Script(name, file);
            this.scripts.add(script);
            // Evaluate the script (start registering listeners)
            script.load();
        }
    }
}
