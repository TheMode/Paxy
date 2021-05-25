package fr.themode.proxy.network;

import fr.themode.proxy.protocol.ClientHandler;
import fr.themode.proxy.protocol.ServerHandler;
import fr.themode.proxy.utils.TaskUtils;

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

    private final WorkerContext workerContext = new WorkerContext();

    public Worker() throws IOException {
        TaskUtils.startSelector(this::threadTick);
    }

    private void threadTick() {
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
                    while (readBuffer.position() < readBuffer.limit()) {
                        final int length = channel.read(readBuffer);
                        if (length == 0) {
                            // Nothing to read
                            break;
                        }
                        if (length == -1) {
                            // EOS
                            throw new IOException("Disconnected");
                        }
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
        var clientContext = new ConnectionContext(serverChannel, new ClientHandler());
        var serverContext = new ConnectionContext(clientChannel, new ServerHandler());

        clientContext.targetConnectionContext = serverContext;
        serverContext.targetConnectionContext = clientContext;

        this.channelMap.put(clientChannel, clientContext);
        this.channelMap.put(serverChannel, serverContext);

        final int interest = SelectionKey.OP_READ;

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, interest);

        serverChannel.configureBlocking(false);
        serverChannel.register(selector, interest);

        this.selector.wakeup();
    }
}
