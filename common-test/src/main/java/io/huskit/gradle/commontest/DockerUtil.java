package io.huskit.gradle.commontest;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.testcontainers.DockerClientFactory;

import java.util.Arrays;
import java.util.HashSet;
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
                        executorService.shutdown();
                    }
                }
            }
        }
    }

    private static void removeContainer(Container container, DockerClient client) {
        var containerId = container.getId();
        client.removeContainerCmd(containerId).withForce(true).withRemoveVolumes(true).exec();
    }

    public List<Container> findHuskitContainers() {
        var client = DockerClientFactory.instance().client();
        var listContainersCmd = client.listContainersCmd().withLabelFilter(Map.of("huskit_container", "true"));
        return listContainersCmd.exec();
    }

    public List<Container> findHuskitContainersWithKey(String key) {
        var client = DockerClientFactory.instance().client();
        var listContainersCmd = client.listContainersCmd().withLabelFilter(
                Map.of(
                        "huskit_container", "true",
                        "huskit_id", key
                )
        );
        return listContainersCmd.exec();
    }

    public List<Container> findHuskitContainersWithIds(String... ids) {
        var idSet = new HashSet<>(Arrays.asList(ids));
        return findHuskitContainers().stream()
                .filter(container -> idSet.contains(container.getLabels().get("huskit_id")))
                .collect(Collectors.toList());
    }
}
