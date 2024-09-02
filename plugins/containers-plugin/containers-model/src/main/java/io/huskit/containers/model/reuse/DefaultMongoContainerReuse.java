package io.huskit.containers.model.reuse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DefaultMongoContainerReuse implements MongoContainerReuse {

    boolean allowed;
    boolean newDatabaseForEachRequest;
    boolean dontStop;
}
