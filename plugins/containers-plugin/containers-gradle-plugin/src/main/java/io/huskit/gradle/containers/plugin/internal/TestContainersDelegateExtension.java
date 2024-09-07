package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TestContainersDelegateExtension {

    private TestContainersDelegate delegate;

    public static String name() {
        return new DefaultInternalExtensionName("huskit_test_containers_delegate").toString();
    }

}
