package fr.themode.proxy.network;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Starts the proxy socket & workers.
 */
public final class Server {

    public static final int WORKER_COUNT = Integer.getInteger("proxy.workers",
            Runtime.getRuntime().availableProcessors() * 2);
    public static final int SOCKET_BUFFER_SIZE = Integer.getInteger("proxy.buffer-size", 262143);
    public static final int MAX_PACKET_SIZE = 2097151; // 3 bytes var-int
    public static final boolean NO_DELAY = true;

    private static final ProxyAddress PROXY_ADDRESS = ProxyAddress.inet("0.0.0.0", 25566);
    private static final ProxyAddress TARGET_ADDRESS = ProxyAddress.inet("0.0.0.0", 25565);

    private final List<Worker> workers = new ArrayList<>(WORKER_COUNT);
    private int index;

    public Server() throws IOException {
        // Create all workers
        for (int i = 0; i < WORKER_COUNT; i++) {
            this.workers.add(new Worker());
        }
        // Start server
        startEndPoint();
    }

    private void startEndPoint() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(PROXY_ADDRESS.socketAddress());
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server starting, wait for connections");
        while (true) {
            // Busy wait for connections
            serverTick(selector, serverSocket);
        }
    }

    private void serverTick(Selector selector, ServerSocketChannel socketChannel) throws IOException {
        selector.select();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        var iter = selectedKeys.iterator();
        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            if (key.isAcceptable()) {
                // Register socket and forward to thread
                Worker thread = findWorker();
                var clientChannel = socketChannel.accept();
                var serverChannel = SocketChannel.open(TARGET_ADDRESS.socketAddress());
                thread.receiveConnection(clientChannel, serverChannel);
                System.out.println("New connection");
            }
            iter.remove();
        }
    }

    private Worker findWorker() {
        this.index = ++index % WORKER_COUNT;
        return workers.get(index);
    }
}
