package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.Action;

public interface ContainerRequestSpecView {

    void image(String image);

    void port(Action<ContainerPortSpecView> portAction);
}
