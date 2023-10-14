package io.huskit.containers.model.request;

import io.huskit.containers.model.reuse.MongoContainerReuse;

public interface MongoRequestedContainer extends RequestedContainer {

    @Override
    MongoContainerReuse containerReuse();

    String databaseName();
}
