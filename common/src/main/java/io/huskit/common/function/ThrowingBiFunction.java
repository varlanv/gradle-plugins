package io.huskit.common.function;

@FunctionalInterface
public interface ThrowingBiFunction<T1, T2, R> {

    R apply(T1 t1, T2 t2) throws Exception;
}
