package io.huskit.containers.model.port;

import java.io.IOException;
import java.net.ServerSocket;

public class DynamicContainerPort implements ContainerPort {

    @Override
    public int number() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
