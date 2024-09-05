package io.huskit.containers.model.reuse;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public final class DefaultMongoContainerReuseOptions implements MongoContainerReuseOptions {

    boolean enabled;
    boolean newDatabaseForEachRequest;
    boolean dontStopOnClose;

    public DefaultMongoContainerReuseOptions(boolean enableAll) {
        this(enableAll, enableAll, enableAll);
    }
}
