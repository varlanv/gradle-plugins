package io.huskit.containers.model.reuse;

public interface MongoContainerReuseOptions extends ContainerReuseOptions {

    boolean newDatabaseForEachRequest();
}
