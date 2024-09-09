package io.huskit.gradle.containers.plugin.api;

import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.internal.spec.mongo.MongoContainerRequestSpec;
import org.gradle.api.Action;

/**
 * Extension for configuring {@link io.huskit.gradle.containers.plugin.HuskitContainersPlugin}.
 * Main user facade for plugin configuration in build.gradle file.
 */
public interface ContainersExtension {

    /**
     * Configures the dependency that containers should start before.
     *
     * @param action action to configure {@link ShouldStartBeforeSpecView}
     */
    void shouldStartBefore(Action<ShouldStartBeforeSpecView> action);

    /**
     * Configures single mongo container.
     * Can be used multiple times to configure multiple (unique) mongo containers.
     *
     * @param action action to configure mongo container {@link MongoContainerRequestSpec}
     */
    void mongo(Action<MongoContainerRequestSpecView> action);
}
