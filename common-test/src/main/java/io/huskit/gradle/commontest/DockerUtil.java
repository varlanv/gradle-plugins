package io.huskit.gradle.commontest;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.testcontainers.DockerClientFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@UtilityClass
public class DockerUtil {

    public void verifyDockerAvailable() {
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new IllegalStateException("Docker is not available. Failing test execution preemptively.");
        }
    }

    @SneakyThrows
    public void cleanupDocker() {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            var client = DockerClientFactory.instance().client();
            var listContainersCmd = client.listContainersCmd().withLabelFilter(Map.of("huskit_container", "true"));
            var containers = listContainersCmd.exec();
            if (!containers.isEmpty()) {
                if (containers.size() == 1) {
                    removeContainer(containers.get(0), client);
                } else {
                    var executorService = Executors.newFixedThreadPool(containers.size());
                    var latch = new CountDownLatch(containers.size());
                    try {
                        for (var container : containers) {
                            executorService.submit(() -> {
                                removeContainer(container, client);
                                latch.countDown();
                            });
                        }
                        latch.await(10, TimeUnit.SECONDS);
                    } finally {
                        executorService.shutdownNow();
                    }
                }
            }
        }
    }

    private static void removeContainer(Container container, DockerClient client) {
        var containerId = container.getId();
        client.removeContainerCmd(containerId).withForce(true).exec();
    }

    public List<Container> findHuskitContainers() {
        var client = DockerClientFactory.instance().client();
        var listContainersCmd = client.listContainersCmd().withLabelFilter(Map.of("huskit_container", "true"));
        return listContainersCmd.exec();
    }

    public List<Container> findHuskitContainersWithId(String id) {
        return findHuskitContainers().stream()
                .filter(container -> container.getLabels().get("huskit_id").equals(id))
                .collect(Collectors.toList());
    }
}
