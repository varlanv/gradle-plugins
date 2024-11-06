package io.huskit.containers.integration;

import io.huskit.common.HtConstants;
import io.huskit.containers.integration.mongo.HtMongo;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.StartedContainersRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

public class DfIntegratedDocker implements HtIntegratedDocker {

    StartedContainersRegistry startedContainersRegistry = new StartedContainersRegistry();

    @Override
    public Map<String, HtStartedContainer> feed(ServicesSpec servicesSpec) {
        var containerSpecs = servicesSpec.containers();
        var result = new LinkedHashMap<String, HtStartedContainer>(containerSpecs.size());
        for (var containerSpec : containerSpecs) {
            var defContainerSpec = (DefContainerSpec) containerSpec;
            if (defContainerSpec.containerType() == ContainerType.MONGO) {
                result.put(
                    defContainerSpec.hash(),
                    startedContainersRegistry.getOrStart(
                        defContainerSpec,
                        spec -> HtMongo.fromImage(defContainerSpec.image().reference())
                            .withContainerSpec(defContainerSpec)
                            .withNewDatabaseForEachRequest(defContainerSpec.booleanProp(HtConstants.Mongo.NEW_DB_EACH_REQUEST))
                            .start()
                    )
                );
            }
        }
        return result;
    }

    @Override
    public void stop() {
        startedContainersRegistry.all().forEach(HtStartedContainer::stopAndRemove);
    }
}
