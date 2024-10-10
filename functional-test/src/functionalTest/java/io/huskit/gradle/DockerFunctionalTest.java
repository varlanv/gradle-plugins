package io.huskit.gradle;

import com.github.dockerjava.api.model.Container;
import io.huskit.gradle.commontest.BaseTest;
import io.huskit.gradle.commontest.DockerUtil;
import io.huskit.gradle.commontest.FunctionalTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.testcontainers.DockerClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(BaseTest.DOCKER_TEST_TAG)
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
        var listContainersCmd = client.listContainersCmd().withLabelFilter(Map.of("HTCT_CONTAINER", "true", "HTCT_GRADLE_ROOT_PROJECT", useCase));
        return new ArrayList<>(listContainersCmd.exec());
    }
}
