package fr.themode.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyThread {

    private final Map<SocketChannel, Context> channelMap = new ConcurrentHashMap<>();
    private final Selector selector = Selector.open();

    public ProxyThread() throws IOException {
        Thread thread = new Thread(() -> {
            ByteBuffer buffer = ByteBuffer.allocate(Server.BUFFER);
            while (true) {
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
                            int length;
                            while ((length = channel.read(buffer)) > 0) {
                                buffer.flip();
                                context.processPackets(target, buffer, length);
                                buffer.clear();
                            }
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
                        }
                    }
                    iter.remove();
                }
            }
        });
        thread.start();
    }

    public void receiveConnection(SocketChannel clientChannel, SocketChannel serverChannel) throws IOException {
        this.channelMap.put(clientChannel, new Context(serverChannel, ConnectionType.CLIENT));
        this.channelMap.put(serverChannel, new Context(clientChannel, ConnectionType.SERVER));

        final int interest = SelectionKey.OP_READ;

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, interest);

        serverChannel.configureBlocking(false);
        serverChannel.register(selector, interest);

        selector.wakeup();
    }

}
