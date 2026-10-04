package com.learning.user_service.support;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/** Integration tests need local Postgres (users_test DB) and Redis. Skip them when either is down. */
public final class LocalInfrastructure {

    private LocalInfrastructure() {
    }

    public static boolean available() {
        return reachable("localhost", 5432) && reachable("localhost", 6379);
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
