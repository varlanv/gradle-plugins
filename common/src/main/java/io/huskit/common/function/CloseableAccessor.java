package io.huskit.common.function;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.atomic.AtomicBoolean;

public interface CloseableAccessor<T extends AutoCloseable> {

    <R> R apply(ThrowingFunction<T, R> mapper);

    void accept(ThrowingConsumer<T> t);

    static <I extends AutoCloseable> CloseableAccessor<I> of(I closeableInstance) {
        return new DfCloseableAccessor<>(closeableInstance, closeableInstance);
    }

    static <I extends AutoCloseable> CloseableAccessor<I> of(I useInstance, AutoCloseable closeInstance) {
        return new DfCloseableAccessor<>(useInstance, closeInstance);
    }
}

@RequiredArgsConstructor
class DfCloseableAccessor<T extends AutoCloseable> implements CloseableAccessor<T> {

    T useInstance;
    AutoCloseable closeInstance;
    AtomicBoolean isClosed = new AtomicBoolean(false);

    @Override
    @SneakyThrows
    public <R> R apply(ThrowingFunction<T, R> mapper) {
        if (isClosed.get()) {
            throw new IllegalStateException(String.format("Instance %s is already closed", useInstance));
        }
        try {
            return mapper.apply(useInstance);
        } finally {
            closeInstance.close();
            isClosed.set(true);
        }
    }

    @Override
    @SneakyThrows
    public void accept(ThrowingConsumer<T> t) {
        if (isClosed.get()) {
            throw new IllegalStateException(String.format("Instance %s is already closed", useInstance));
        }
        try {
            t.accept(useInstance);
        } finally {
            closeInstance.close();
            isClosed.set(true);
        }
    }
}
