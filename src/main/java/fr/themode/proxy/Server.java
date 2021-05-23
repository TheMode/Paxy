package fr.themode.proxy;

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

public class Server {

    public static final int THREAD_COUNT = Integer.getInteger("proxy.threads", Runtime.getRuntime().availableProcessors());
    public static final int BUFFER = Integer.getInteger("proxy.buffer", 2097151);
    public static final int SELECTOR_TIMER = Integer.getInteger("proxy.timer", 50);

    private static final InetSocketAddress PROXY_ADDRESS = new InetSocketAddress("0.0.0.0", 25566);
    private static final InetSocketAddress TARGET_ADDRESS = new InetSocketAddress("0.0.0.0", 25565);

    private final List<ProxyThread> threads = new ArrayList<>(THREAD_COUNT);
    private int index;

    public Server() throws IOException {
        // Create all threads
        for (int i = 0; i < THREAD_COUNT; i++) {
            this.threads.add(new ProxyThread());
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
                    index = ++index % THREAD_COUNT;
                    ProxyThread thread = threads.get(index);

                    var clientChannel = serverSocket.accept();
                    var serverChannel = SocketChannel.open(TARGET_ADDRESS);
                    thread.receiveConnection(clientChannel, serverChannel);
                    System.out.println("New connection");
                }
                iter.remove();
            }
        }
    }
}
