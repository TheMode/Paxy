package fr.themode.proxy.network;

import fr.themode.proxy.protocol.Protocol;

import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Contains objects that we can shared across all the connection of a {@link Worker worker}.
 */
public final class WorkerContext {

    /**
     * Stores data read from the socket.
     */
    public final ByteBuffer readBuffer = ByteBuffer.allocateDirect(Server.THREAD_READ_BUFFER);

    /**
     * Stores data to write to the socket.
     */
    public final ByteBuffer writeBuffer = ByteBuffer.allocateDirect(Server.THREAD_WRITE_BUFFER);

    /**
     * Stores a single packet payload to be read.
     * <p>
     * Should be used by {@link Protocol#read(ConnectionContext, ByteBuffer, WorkerContext)}.
     */
    public final ByteBuffer contentBuffer = ByteBuffer.allocateDirect(Server.MAX_PACKET_SIZE);

    public final ByteBuffer transformPayload = ByteBuffer.allocateDirect(Server.MAX_PACKET_SIZE);
    public final ByteBuffer transform = ByteBuffer.allocateDirect(Server.MAX_PACKET_SIZE);

    public final Deflater deflater = new Deflater();
    public final Inflater inflater = new Inflater();

    public void clearBuffers() {
        this.readBuffer.clear();
        this.writeBuffer.clear();
        this.contentBuffer.clear();
        this.transformPayload.clear();
        this.transform.clear();
    }
}
