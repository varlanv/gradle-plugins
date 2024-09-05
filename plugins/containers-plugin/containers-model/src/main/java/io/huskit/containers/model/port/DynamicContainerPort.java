package io.huskit.containers.model.port;

import io.huskit.gradle.common.function.MemoizedSupplier;

import java.io.IOException;
import java.net.ServerSocket;

public final class DynamicContainerPort implements ContainerPort {

    MemoizedSupplier<Integer> number = new MemoizedSupplier<>(this::randomPort);

    @Override
    public int number() {
        return number.get();
    }

    private int randomPort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
