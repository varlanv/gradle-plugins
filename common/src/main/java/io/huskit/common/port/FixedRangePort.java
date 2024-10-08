package io.huskit.common.port;

import io.huskit.common.Volatile;
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
    MemoizedSupplier<Integer> value = MemoizedSupplier.of(this::randomPort);

    @Override
    public Integer hostValue() {
        return value.get();
    }

    @Override
    public Optional<Integer> containerValue() {
        return Optional.of(containerValue);
    }

    @Override
    public Boolean isFixed() {
        return true;
    }

    private int randomPort() {
        var maxAttempts = 10;
        var exception = Volatile.<Exception>of();
        for (var i = 0; i < maxAttempts; i++) {
            try (var socket = new ServerSocket(ThreadLocalRandom.current().nextInt(rangeFrom, rangeTo + 1))) {
                return socket.getLocalPort();
            } catch (IOException e) {
                exception.set(e);
            }
        }
        var exceptionMessage = String.format("Could not find an available port in the range [%s] to [%s]", rangeFrom, rangeTo);
        throw exception.maybe()
                .map(e -> new IllegalStateException(exceptionMessage, e))
                .orElseGet(() -> new IllegalStateException(exceptionMessage));
    }
}
