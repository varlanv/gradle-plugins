package io.huskit.common.function;

@FunctionalInterface
public interface ThrowingRunnable {

    void run() throws Exception;
}
