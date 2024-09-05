package io.huskit.gradle.common.function;

public interface ThrowingFunction<T, R> {

    R apply(T t) throws Exception;
}
