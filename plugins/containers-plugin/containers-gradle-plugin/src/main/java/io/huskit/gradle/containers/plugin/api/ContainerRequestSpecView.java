package io.huskit.gradle.containers.plugin.api;

public interface ContainerRequestSpecView {

    void image(String image);

    void fixedPort(Integer port);
}
