package io.huskit.containers.model;

import io.huskit.containers.model.started.StartedContainer;

import java.util.List;

public interface Containers {

    List<StartedContainer> start();
}
