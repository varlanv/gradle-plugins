package io.huskit.containers.model.reuse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultMongoContainerReuse implements MongoContainerReuse {

    private final boolean allowed;
    private final boolean newDatabaseForEachRequest;
    private final boolean dontStop;

    @Override
    public boolean allowed() {
        return allowed;
    }

    @Override
    public boolean dontStop() {
        return dontStop;
    }

    @Override
    public boolean newDatabaseForEachRequest() {
        return newDatabaseForEachRequest;
    }
}
