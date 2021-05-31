package fr.themode.proxy.network;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;

public record ProxyAddress(SocketAddress socketAddress) {
    public static ProxyAddress inet(String host, int port) {
        return new ProxyAddress(new InetSocketAddress(host, port));
    }

    public static ProxyAddress unix(String path) {
        return new ProxyAddress(UnixDomainSocketAddress.of(path));
    }
}
