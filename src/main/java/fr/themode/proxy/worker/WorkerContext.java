package fr.themode.proxy.worker;

import fr.themode.proxy.Server;
import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.protocol.Protocol;
import fr.themode.proxy.script.ScriptLocal;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Contains objects that we can shared across all the connection of a {@link Worker worker}.
 */
public final class WorkerContext {

    /**
     * Stores data read from the socket.
     */
    public final ByteBuffer readBuffer = allocate(Server.SOCKET_BUFFER_SIZE);

    /**
     * Stores data to write to the socket.
     */
    public final ByteBuffer writeBuffer = allocate(Server.SOCKET_BUFFER_SIZE);

    /**
     * Stores a single packet payload to be read.
     * <p>
     * Should be used by {@link Protocol#read(ConnectionContext, ByteBuffer, ByteBuffer, WorkerContext)}.
     */
    public final ByteBuffer contentBuffer = allocate(Server.MAX_PACKET_SIZE);

    public final ByteBuffer transformPayload = allocate(Server.MAX_PACKET_SIZE);
    public final ByteBuffer transform = allocate(Server.MAX_PACKET_SIZE);

    public final Deflater deflater = new Deflater();
    public final Inflater inflater = new Inflater();

    public final ScriptLocal scriptLocal = ScriptLocal.load("scripts");

    public void clearBuffers() {
        this.readBuffer.clear();
        this.writeBuffer.clear();
        this.contentBuffer.clear();
        this.transformPayload.clear();
        this.transform.clear();
    }

    private static ByteBuffer allocate(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }
}
