package com.learning.user_service.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Integration tests need a local Redis (the database is in-memory H2). Skip them when it is down. */
public final class LocalInfrastructure {

    private LocalInfrastructure() {
    }

    public static boolean available() {
        return reachable("localhost", 6379);
    }

    private static boolean reachable(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 300);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
