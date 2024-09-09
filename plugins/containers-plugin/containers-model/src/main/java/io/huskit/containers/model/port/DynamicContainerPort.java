package io.huskit.containers.model.port;

import io.huskit.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

@RequiredArgsConstructor
public final class DynamicContainerPort implements ContainerPort {

    MemoizedSupplier<Integer> value = new MemoizedSupplier<>(this::randomPort);

    @Override
    public Integer hostValue() {
        return value.get();
    }

    @Override
    public Optional<Integer> containerValue() {
        return Optional.empty();
    }

    @Override
    public Boolean isFixed() {
        return false;
    }

    private int randomPort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
