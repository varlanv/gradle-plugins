package io.huskit.gradle

import com.github.dockerjava.api.model.Container
import io.huskit.gradle.commontest.BaseFunctionalSpec
import io.huskit.gradle.containers.plugin.internal.MongoContainerId
import org.testcontainers.DockerClientFactory

import java.util.stream.Collectors

abstract class BaseDockerFunctionalSpec extends BaseFunctionalSpec {

    def setup() {
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new IllegalStateException("Docker is not available. Failing test execution preemptively.")
        }
    }

    def cleanup() {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            def client = DockerClientFactory.instance().client()
            def listContainersCmd = client.listContainersCmd().withLabelFilter(["huskit_container": "true"])
            def containers = listContainersCmd.exec()
            for (def container : containers) {
                def containerId = container.getId()
                client.stopContainerCmd(containerId).exec()
                client.removeContainerCmd(containerId).exec()
            }
        }
    }

    List<Container> findHuskitContainers() {
        def client = DockerClientFactory.instance().client()
        def listContainersCmd = client.listContainersCmd().withLabelFilter(["huskit_container": "true"])
        return listContainersCmd.exec()
    }

    List<Container> findHuskitContainersForUseCase(String useCase) {
        def client = DockerClientFactory.instance().client()
        def listContainersCmd = client.listContainersCmd().withLabelFilter(["huskit_container": "true"])
        return listContainersCmd.exec().stream()
                .filter({ container ->
                    def idJson = container.getLabels().get("huskit_id")
                    if (idJson == null) {
                        return false
                    }
                    return mapper().readTree(idJson).get(MongoContainerId.Fields.rootProjectName).asText() == useCase
                })
                .collect(Collectors.toList())
    }
}
