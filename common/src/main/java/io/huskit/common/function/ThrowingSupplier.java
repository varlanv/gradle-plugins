package io.huskit.common.function;

public interface ThrowingSupplier<T> {

    T get() throws Exception;
}
