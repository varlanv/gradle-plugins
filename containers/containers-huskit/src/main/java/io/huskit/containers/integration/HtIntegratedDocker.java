package io.huskit.containers.integration;

import java.util.List;
import java.util.Map;

public interface HtIntegratedDocker {

    Map<String, HtStartedContainer> feed(ServicesSpec servicesSpec);

    default Map<String, HtStartedContainer> feed(List<ContainerSpec> specs) {
        return feed(ServicesSpec.from(specs));
    }

    static HtIntegratedDocker instance() {
        return new DfIntegratedDocker();
    }

    void stop();
}
