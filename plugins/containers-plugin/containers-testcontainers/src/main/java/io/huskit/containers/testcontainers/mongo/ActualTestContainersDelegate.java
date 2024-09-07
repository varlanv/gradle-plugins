package io.huskit.containers.testcontainers.mongo;

import io.huskit.containers.model.ExistingContainer;
import io.huskit.containers.model.id.ContainerId;
import lombok.SneakyThrows;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ActualTestContainersDelegate implements TestContainersDelegate, Serializable {

    @Override
    @SneakyThrows
    public <T extends GenericContainer<?>> void execInContainer(Supplier<T> container, String... command) {
        container.get().execInContainer(command);
    }

    @Override
    public <T extends GenericContainer<?>> void stop(Supplier<T> container) {
        container.get().stop();
    }

    @Override
    public <T extends GenericContainer<?>> int getFirstMappedPort(Supplier<T> container) {
        return container.get().getFirstMappedPort();
    }

    @Override
    public <T extends GenericContainer<?>> void start(T container) {
        container.start();
    }

    @Override
    public String getConnectionString(Supplier<MongoDBContainer> mongoDBContainerSupplier) {
        return mongoDBContainerSupplier.get().getConnectionString();
    }

    @Override
    public Optional<ExistingContainer> getExistingContainer(ContainerId id) {
        var client = DockerClientFactory.instance().client();
        var idJson = id.json();
        var huskitId = client.listContainersCmd().withLabelFilter(Map.of("huskit_id", idJson)).exec();
        if (huskitId.size() == 1) {
            var container = huskitId.get(0);
            var labels = container.getLabels();
            return Optional.of(
                    new ExistingContainer(
                            idJson,
                            container.getId(),
                            Duration.ofSeconds(container.getCreated()).toMillis(),
                            labels
                    )
            );
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void remove(ExistingContainer existingContainer) {
        DockerClientFactory.instance().client()
                .removeContainerCmd(existingContainer.containerId())
                .withRemoveVolumes(true)
                .withForce(true)
                .exec();
    }
}
