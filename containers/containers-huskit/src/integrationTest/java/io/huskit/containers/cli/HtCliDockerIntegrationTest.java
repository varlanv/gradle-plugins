package io.huskit.containers.cli;

import com.github.dockerjava.api.DockerClient;
import io.huskit.containers.api.HtDocker;
import io.huskit.gradle.commontest.IntegrationTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class HtCliDockerIntegrationTest implements IntegrationTest {

    String helloWorldImage = "hello-world";

    @Test
    void list_no_exception() {
        HtCliDocker subject = HtDocker.cli();
        var containers = subject.listContainers().withArgs(argsSpec ->
                        argsSpec.withAll()
                                .build())
                .asList();

        assertThat(containers).isNotNull();
    }

    @RepeatedTest(1)
    void run__with_labels__should_add_labels() {
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

    @RepeatedTest(1)
    void tst_gith() {
        DockerClient dockerClient = DockerClientFactory.instance().client();
        var labels = Map.of("someLabelKey", "someLabelVal");
        var container = dockerClient.createContainerCmd(helloWorldImage)
                .withLabels(labels)
                .exec();
        dockerClient.startContainerCmd(container.getId()).exec();
        var inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
1
        try {
            assertThat(container).isNotNull();
            assertThat(container.getId()).isNotBlank();
            assertThat(inspectContainerResponse.getConfig().getLabels()).isEqualTo(labels);
        } finally {
            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
        }
    }

//    @Test
    void s() throws ExecutionException, InterruptedException {
        var http = HttpClient.newBuilder().build();
        http.sendAsync(
                        HttpRequest.newBuilder()
                                .GET()
                                .uri(URI.create("https://www.google.com"))
                                .build(),
                        HttpResponse.BodyHandlers.ofString()
                ).thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .get();
    }
}
