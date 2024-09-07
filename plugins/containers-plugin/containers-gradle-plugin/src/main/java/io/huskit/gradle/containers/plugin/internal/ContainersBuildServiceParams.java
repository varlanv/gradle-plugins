package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import lombok.RequiredArgsConstructor;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.tasks.Internal;

@RequiredArgsConstructor
public abstract class ContainersBuildServiceParams implements BuildServiceParameters {

    @Internal
    public abstract Property<TestContainersDelegate> getTestContainersDelegate();
}
