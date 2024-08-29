package io.huskit.gradle.commontest

import org.testcontainers.DockerClientFactory

abstract class BaseDockerFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new IllegalStateException("Docker is not available. Failing test execution preemptively.")
        }
    }
}
