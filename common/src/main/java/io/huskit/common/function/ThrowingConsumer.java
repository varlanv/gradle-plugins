package io.huskit.common.function;

public interface ThrowingConsumer<T> {

    void accept(T t) throws Exception;
}
