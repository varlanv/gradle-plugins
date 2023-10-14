package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.Action;

public interface ContainersExtension {

    static String name() {
        return "serviceContainers";
    }

    void shouldStartBefore(Action<ShouldStartBefore> action);

    void mongo(Action<MongoContainerRequestedByUser> action);
}
