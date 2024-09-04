
package io.huskit.gradle.commontest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.atomic.AtomicBoolean;

public interface DockerIntegrationTest extends IntegrationTest {

    AtomicBoolean IS_INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void cleanupDockerOnce() {
        if (IS_INITIALIZED.compareAndSet(false, true)) {
            DockerUtil.cleanupDocker();
        }
    }

    @BeforeEach
    default void setupDocker() {
        DockerUtil.verifyDockerAvailable();
    }

    @AfterEach
    default void cleanupDocker() {
        DockerUtil.cleanupDocker();
    }
}
