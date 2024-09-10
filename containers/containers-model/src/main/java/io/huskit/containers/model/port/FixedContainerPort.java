package io.huskit.containers.model.port;

import io.huskit.common.function.MemoizedSupplier;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public final class FixedContainerPort implements ContainerPort {

    Integer hostValue;
    @NonNull
    Integer containerValue;
    MemoizedSupplier<Integer> value = new MemoizedSupplier<>(this::portValue);

    @Override
    public Integer hostValue() {
        return value.get();
    }

    @Override
    public Optional<Integer> containerValue() {
        return Optional.of(containerValue);
    }

    private Integer portValue() {
        try (var socket = new ServerSocket(hostValue)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Boolean isFixed() {
        return true;
    }
}
