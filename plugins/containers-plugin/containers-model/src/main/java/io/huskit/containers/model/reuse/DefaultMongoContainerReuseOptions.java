package io.huskit.containers.model.reuse;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public final class DefaultMongoContainerReuseOptions implements MongoContainerReuseOptions {

    Boolean enabled;
    Boolean newDatabaseForEachRequest;
    Boolean reuseBetweenBuilds;
    ContainerCleanupOptions cleanup;
}
