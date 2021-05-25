package fr.themode.proxy.network;

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
     * Stores a single packet payload to be processed.
     */
    public final ByteBuffer contentBuffer = ByteBuffer.allocateDirect(Server.THREAD_CONTENT_BUFFER);

    public final Deflater deflater = new Deflater();
    public final Inflater inflater = new Inflater();

    public void clearBuffers() {
        this.readBuffer.clear();
        this.writeBuffer.clear();
        this.contentBuffer.clear();
    }
}
