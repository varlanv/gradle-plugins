
package io.huskit.gradle.commontest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseDockerIntegrationTest extends BaseIntegrationTest {

    private static final AtomicBoolean IS_INITIALIZED = new AtomicBoolean(false);

    @BeforeAll
    static void cleanupDockerOnce() {
        if (IS_INITIALIZED.compareAndSet(false, true)) {
            DockerUtil.cleanupDocker();
        }
    }

    @BeforeEach
    void setupDocker() {
        DockerUtil.verifyDockerAvailable();
    }

    @AfterEach
    void cleanupDocker() {
        DockerUtil.cleanupDocker();
    }
}
