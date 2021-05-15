package fr.themode.proxy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Main {

    public static void main(String[] args) throws IOException {
        new Server();
    }

    private static void answerWithEcho(ByteBuffer buffer, SelectionKey key)
            throws IOException {

        SocketChannel client = (SocketChannel) key.channel();
        client.read(buffer);
        buffer.flip();
        client.write(buffer);
        buffer.clear();
    }

}
