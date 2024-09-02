package io.huskit.gradle.common.plugin.model;

public interface BuildEndAware extends AutoCloseable {

    void onBuildEnd() throws Exception;

    @Override
    default void close() throws Exception {
        onBuildEnd();
    }
}
