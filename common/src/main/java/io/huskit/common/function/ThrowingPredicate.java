package io.huskit.common.function;

@FunctionalInterface
public interface ThrowingPredicate<T> {

    boolean test(T t) throws Exception;
}
