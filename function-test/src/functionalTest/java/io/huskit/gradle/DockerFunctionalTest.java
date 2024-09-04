package io.huskit.gradle;

import com.github.dockerjava.api.model.Container;
import io.huskit.containers.model.id.MongoContainerId;
import io.huskit.gradle.commontest.DockerUtil;
import io.huskit.gradle.commontest.FunctionalTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.DockerClientFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface DockerFunctionalTest extends FunctionalTest {

    @BeforeEach
    default void setupDocker() {
        DockerUtil.verifyDockerAvailable();
    }

    @AfterEach
    default void cleanupDocker() {
        DockerUtil.cleanupDocker();
    }

    default List<Container> findHuskitContainers() {
        return DockerUtil.findHuskitContainers();
    }

    default List<Container> findHuskitContainersForUseCase(String useCase) {
        var client = DockerClientFactory.instance().client();
        var listContainersCmd = client.listContainersCmd().withLabelFilter(Map.of("huskit_container", "true"));
        return listContainersCmd.exec().stream()
                .filter(container -> {
                    var idJson = container.getLabels().get("huskit_id");
                    if (idJson == null) {
                        return false;
                    }
                    return getJsonField(idJson, MongoContainerId.Fields.rootProjectName, String.class).equals(useCase);
                })
                .collect(Collectors.toList());
    }
}
