package fr.themode.proxy.script;

import fr.themode.proxy.MinecraftBuffer;
import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.packet.Packet;
import fr.themode.proxy.utils.FileUtils;
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
        final var registry = context.getState().registry();
        Packet packet = registry.getPacket(bound, packetId);
        if (packet == null) {
            // No provider available
            return false;
        }
        scripts.forEach(script -> script.getExecutor().run(context, bound, packetId, packet, MinecraftBuffer.wrap(in)));
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
            if (file.isDirectory()) {
                continue;
            }
            Script script = new Script(FileUtils.readFile(file));
            this.scripts.add(script);
            // Evaluate the script (start registering listeners)
            script.load();
        }
    }
}
