package io.huskit.containers.cli;

import com.github.dockerjava.api.DockerClient;
import io.huskit.containers.api.HtDocker;
import io.huskit.gradle.commontest.IntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HtCliDockerIntegrationTest implements IntegrationTest {

    String helloWorldImage = "hello-world";

    @Test
    @Disabled
    void list_no_exception() {
        HtCliDocker subject = HtDocker.cli();
        var containers = subject.listContainers().withArgs(argsSpec ->
                        argsSpec.withAll()
                                .build())
                .asList();

        assertThat(containers).isNotNull();
    }

    @Disabled
    @RepeatedTest(1)
    void tst_cli() {
        HtCliDocker subject = HtDocker.cli();
        var labels = Map.of("someLabelKey", "someLabelVal");
        var container = subject.run(helloWorldImage)
                .withOptions(options -> options.withLabels(labels))
                .exec();

        try {
            assertThat(container).isNotNull();
            assertThat(container.id()).isNotBlank();
            assertThat(container.labels()).isEqualTo(labels);
        } finally {
            subject.remove(container.id()).withForce().exec();
        }
    }

    @Disabled
    @RepeatedTest(1)
    void tst_github() {
        DockerClient dockerClient = DockerClientFactory.instance().client();
        var labels = Map.of("someLabelKey", "someLabelVal");
        var container = dockerClient.createContainerCmd(helloWorldImage)
                .withLabels(labels)
                .exec();
        dockerClient.startContainerCmd(container.getId()).exec();
        var inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();

        try {
            assertThat(container).isNotNull();
            assertThat(container.getId()).isNotBlank();
            assertThat(inspectContainerResponse.getConfig().getLabels()).isEqualTo(labels);
        } finally {
            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
        }
    }
}
