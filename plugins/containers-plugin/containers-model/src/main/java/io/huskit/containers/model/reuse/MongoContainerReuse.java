package io.huskit.containers.model.reuse;

public interface MongoContainerReuse extends ContainerReuse {

    boolean newDatabaseForEachRequest();
}
