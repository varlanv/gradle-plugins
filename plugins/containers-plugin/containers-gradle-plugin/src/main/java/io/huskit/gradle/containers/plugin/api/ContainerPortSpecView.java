package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.Action;

public interface ContainerPortSpecView {

    void fixed(Action<FixedContainerPortSpecView> action);
}
