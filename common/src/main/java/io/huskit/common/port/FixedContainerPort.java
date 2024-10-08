package io.huskit.common.port;

import io.huskit.common.function.MemoizedSupplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.net.ServerSocket;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public final class FixedContainerPort implements ContainerPort {

    Integer hostValue;
    @NonNull
    Integer containerValue;
    MemoizedSupplier<Integer> value = MemoizedSupplier.of(this::portValue);

    @Override
    public Integer hostValue() {
        return value.get();
    }

    @Override
    public Optional<Integer> containerValue() {
        return Optional.of(containerValue);
    }

    @SneakyThrows
    private Integer portValue() {
        try (var socket = new ServerSocket(hostValue)) {
            return socket.getLocalPort();
        }
    }

    @Override
    public Boolean isFixed() {
        return true;
    }
}
