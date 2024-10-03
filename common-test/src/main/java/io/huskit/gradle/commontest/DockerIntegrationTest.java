package io.huskit.gradle.commontest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;

@Tag(BaseTest.DOCKER_TEST_TAG)
@ExtendWith(DockerAvailableCondition.class)
public interface DockerIntegrationTest extends IntegrationTest {

    AtomicBoolean IS_INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    default void cleanupDockerOnce() {
        if (IS_INITIALIZED.compareAndSet(false, true)) {
            DockerUtil.cleanupDocker();
        }
    }

    @BeforeAll
    default void setupDocker() {
        DockerUtil.verifyDockerAvailable();
    }

    @AfterEach
    default void cleanupDocker() {
        DockerUtil.cleanupDocker();
    }
}
