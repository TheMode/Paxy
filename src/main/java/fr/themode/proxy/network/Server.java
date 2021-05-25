package fr.themode.proxy.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Starts the proxy socket & workers.
 */
public final class Server {

    public static final int WORKER_COUNT = Integer.getInteger("proxy.workers", Runtime.getRuntime().availableProcessors() * 2);
    public static final int THREAD_READ_BUFFER = Integer.getInteger("proxy.thread-read-buffer", 262143);
    public static final int THREAD_WRITE_BUFFER = Integer.getInteger("proxy.thread-write-buffer", 262143);
    public static final int THREAD_CONTENT_BUFFER = 2097151; // Max size of a 3 bytes var-int
    public static final int SELECTOR_TIMER = Integer.getInteger("proxy.timer", 10);

    private static final InetSocketAddress PROXY_ADDRESS = new InetSocketAddress("0.0.0.0", 25566);
    private static final InetSocketAddress TARGET_ADDRESS = new InetSocketAddress("0.0.0.0", 25565);

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
        serverSocket.bind(PROXY_ADDRESS);
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server starting, wait for connections");
        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (key.isAcceptable()) {
                    // Register socket and forward to thread
                    Worker thread = findWorker();

                    var clientChannel = serverSocket.accept();
                    var serverChannel = SocketChannel.open(TARGET_ADDRESS);
                    thread.receiveConnection(clientChannel, serverChannel);
                    System.out.println("New connection");
                }
                iter.remove();
            }
        }
    }

    private Worker findWorker() {
        this.index = ++index % WORKER_COUNT;
        return workers.get(index);
    }
}