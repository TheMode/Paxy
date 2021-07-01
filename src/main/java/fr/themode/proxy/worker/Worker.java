package fr.themode.proxy.worker;

import fr.themode.proxy.PacketBound;
import fr.themode.proxy.Server;
import fr.themode.proxy.connection.ConnectionContext;
import fr.themode.proxy.protocol.ProtocolHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A worker with its own {@link Selector selector}.
 * <p>
 * Used to read and process packets.
 */
public class Worker {

    private final Map<SocketChannel, ConnectionContext> channelMap = new ConcurrentHashMap<>();
    private final Selector selector = Selector.open();

    public Worker() throws IOException {
        WorkerThread.start(this::threadTick);
    }

    private void threadTick(WorkerContext workerContext) {
        try {
            selector.select();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iter = selectedKeys.iterator();
        while (iter.hasNext()) {
            SelectionKey key = iter.next();

            SocketChannel channel = (SocketChannel) key.channel();
            if (!channel.isOpen()) {
                iter.remove();
                continue;
            }

            if (key.isReadable()) {
                var context = channelMap.get(channel);
                var target = context.getTarget();
                try {
                    ByteBuffer readBuffer = workerContext.readBuffer;

                    // Consume last incomplete packet
                    context.consumeCache(readBuffer);

                    // Read socket
                    if (channel.read(readBuffer) == -1) {
                        // EOS
                        throw new IOException("Disconnected");
                    }

                    // Process data
                    readBuffer.flip();
                    context.processPackets(target, workerContext);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        // Client close
                        channel.close();
                        target.close();
                        channelMap.remove(channel);
                        channelMap.remove(target);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } finally {
                    workerContext.clearBuffers();
                }
            }
            iter.remove();
        }
    }

    public void receiveConnection(SocketChannel clientChannel, SocketChannel serverChannel) throws IOException {
        var clientContext = new ConnectionContext(serverChannel, ProtocolHandler.CLIENT, PacketBound.IN);
        var serverContext = new ConnectionContext(clientChannel, ProtocolHandler.SERVER, PacketBound.OUT);

        clientContext.setTargetConnectionContext(serverContext);
        serverContext.setTargetConnectionContext(clientContext);

        this.channelMap.put(clientChannel, clientContext);
        this.channelMap.put(serverChannel, serverContext);

        register(clientChannel);
        register(serverChannel);
        this.selector.wakeup();
    }

    private void register(SocketChannel channel) throws IOException {
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        var socket = channel.socket();
        socket.setSendBufferSize(Server.SOCKET_BUFFER_SIZE);
        socket.setReceiveBufferSize(Server.SOCKET_BUFFER_SIZE);
        socket.setTcpNoDelay(Server.NO_DELAY);
    }
}
