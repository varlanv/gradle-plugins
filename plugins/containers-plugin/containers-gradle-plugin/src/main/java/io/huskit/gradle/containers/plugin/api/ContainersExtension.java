package io.huskit.gradle.containers.plugin.api;

import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpec;
import org.gradle.api.Action;

/**
 * Extension for configuring {@link io.huskit.gradle.containers.plugin.HuskitContainersPlugin}.
 * Main user facade for plugin configuration in build.gradle file.
 */
public interface ContainersExtension {

    /**
     * Default extension name.
     *
     * @return extension name
     */
    static String name() {
        return "huskitContainers";
    }

    /**
     * Configures the dependency that containers should start before.
     *
     * @param action action to configure {@link ShouldStartBeforeSpec}
     */
    void shouldStartBefore(Action<ShouldStartBeforeSpec> action);

    /**
     * Configures single mongo container.
     * Can be used multiple times to configure multiple (unique) mongo containers.
     *
     * @param action action to configure mongo container {@link MongoContainerRequestSpec}
     */
    void mongo(Action<MongoContainerRequestSpec> action);
}
