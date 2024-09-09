package io.huskit.containers.model.port;

import io.huskit.common.function.MemoizedSupplier;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class FixedRangePort implements ContainerPort {

    Integer rangeFrom;
    Integer rangeTo;
    @NonNull
    Integer containerValue;
    MemoizedSupplier<Integer> value = new MemoizedSupplier<>(this::randomPort);

    @Override
    public Integer hostValue() {
        return value.get();
    }

    public Optional<Integer> containerValue() {
        return Optional.of(containerValue);
    }

    @Override
    public Boolean isFixed() {
        return true;
    }

    private int randomPort() {
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            try (var socket = new ServerSocket(ThreadLocalRandom.current().nextInt(rangeFrom, rangeTo + 1))) {
                return socket.getLocalPort();
            } catch (IOException e) {
                // ignore
            }
        }
        throw new IllegalStateException(String.format("Could not find an available port in the range [%s] to [%s]", rangeFrom, rangeTo));
    }
}
