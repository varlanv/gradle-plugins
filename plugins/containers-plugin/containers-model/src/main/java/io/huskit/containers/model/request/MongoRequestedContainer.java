package io.huskit.containers.model.request;

import io.huskit.containers.model.reuse.MongoContainerReuseOptions;

public interface MongoRequestedContainer extends RequestedContainer {

    @Override
    MongoContainerReuseOptions reuseOptions();

    String databaseName();
}
