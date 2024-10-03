package io.huskit.gradle.commontest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

@Tag(BaseTest.DOCKER_TEST_TAG)
@ExtendWith(DockerAvailableCondition.class)
public interface DockerIntegrationTest extends IntegrationTest {

    @BeforeAll
    default void setupDocker() {
        DockerUtil.verifyDockerAvailable();
    }
}
