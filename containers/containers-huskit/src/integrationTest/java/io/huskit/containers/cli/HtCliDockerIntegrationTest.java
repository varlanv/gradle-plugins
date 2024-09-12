package io.huskit.containers.cli;

import io.huskit.containers.api.HtDocker;
import io.huskit.gradle.commontest.IntegrationTest;
import org.junit.jupiter.api.Test;

public class HtCliDockerIntegrationTest implements IntegrationTest {

    @Test
    void d() {
        var subject = HtDocker.cli();

//        var htPs = subject.listContainers().withArgs(argsSpec ->
//                argsSpec.all()
//                        .filter(filterSpec -> filterSpec.id("a"))
//                        .filter(filterSpec -> filterSpec.name("a"))
//        );
    }
}
