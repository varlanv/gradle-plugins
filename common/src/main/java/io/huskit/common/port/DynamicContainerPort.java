package io.huskit.common.port;

import io.huskit.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.net.ServerSocket;
import java.util.Optional;

@RequiredArgsConstructor
public final class DynamicContainerPort implements ContainerPort {

    MemoizedSupplier<Integer> value = MemoizedSupplier.of(this::randomPort);

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

    @SneakyThrows
    private int randomPort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Override
    public int hashCode() {
        return value.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DynamicContainerPort && value.get().equals(((DynamicContainerPort) obj).value.get());
    }
}
