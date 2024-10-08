package io.huskit.containers.testcontainers.mongo;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.DefaultExistingContainer;
import io.huskit.containers.model.ExistingContainer;
import io.huskit.containers.model.HtConstants;
import io.huskit.containers.model.id.ContainerKey;
import io.huskit.log.Log;
import io.huskit.log.ProfileLog;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.io.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ActualTestContainersDelegate implements TestContainersDelegate, Serializable {

    private static final AtomicBoolean reuseInitialized = new AtomicBoolean();
    private static final Supplier<DockerClient> dockerClient = MemoizedSupplier.of(() ->
            ProfileLog.withProfile("Testcontainers initialize", () ->
                    DockerClientFactory.instance().client()));
    Log log;

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
    public <T extends GenericContainer<?>> Integer getFirstMappedPort(Supplier<T> container) {
        return container.get().getFirstMappedPort();
    }

    @Override
    public <T extends GenericContainer<?>> void start(T container) {
        container.start();
    }

    @Override
    public String getConnectionString(Supplier<MongoDBContainer> mongoDbContainerSupplier) {
        return mongoDbContainerSupplier.get().getConnectionString();
    }

    @Override
    public Optional<ExistingContainer> getExistingContainer(ContainerKey key) {
        var keyJson = key.json();
        try (var listContainersCmd = dockerClient.get().listContainersCmd();) {
            var huskitContainers = listContainersCmd.withLabelFilter(Map.of(HtConstants.KEY_LABEL, keyJson)).exec();
            if (huskitContainers.size() == 1) {
                var container = huskitContainers.get(0);
                var labels = container.getLabels();
                return Optional.of(
                        new DefaultExistingContainer(
                                keyJson,
                                container.getId(),
                                Duration.ofSeconds(container.getCreated()).toMillis(),
                                labels
                        )
                );
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public void remove(ExistingContainer existingContainer) {
        try (var removeContainerCmd = dockerClient.get()
                .removeContainerCmd(existingContainer.containerKey())) {
            removeContainerCmd
                    .withRemoveVolumes(true)
                    .withForce(true)
                    .exec();
        }
    }

    /**
     * Enables reuse of TestContainers containers.
     * Testcontainers library requires property {@code testcontainers.reuse.enable} to be set to {@code true} in
     * {@code ~/.testcontainers.properties} file.
     */
    @Override
    @SneakyThrows
    public void setReuse() {
        if (!reuseInitialized.get()) {
            synchronized (this) {
                if (!reuseInitialized.get()) {
                    var userHomePath = System.getProperty("user.home");
                    var propertiesFile = getTestcontainerPropertiesFile(userHomePath);
                    var props = new Properties();
                    try (var reader = new FileReader(propertiesFile)) {
                        props.load(reader);
                    }
                    var reuseKey = "testcontainers.reuse.enable";
                    if (!"true".equals(props.getProperty(reuseKey))) {
                        props.put(reuseKey, "true");
                        try (var writer = new FileWriter(propertiesFile)) {
                            props.store(writer, "Modified by huskit-containers plugin");
                        }
                        log.lifecycle("Enabled property [{}] in file [{}] for TestContainers containers reuse. See https://java.testcontainers.org/features/reuse/",
                                reuseKey, propertiesFile);
                        reuseInitialized.set(true);
                    }
                }
            }
        }
    }

    public List<Map<String, String>> findHuskitContainers() {
        try (var listContainersCmd = dockerClient.get().listContainersCmd().withLabelFilter(Map.of("huskit_container", "true"))) {
            return listContainersCmd.exec().stream()
                    .map(Container::getLabels)
                    .collect(Collectors.toList());
        }
    }


    private File getTestcontainerPropertiesFile(String userHomePath) throws IOException {
        var userHome = new File(userHomePath);
        var properties = new File(userHome, ".testcontainers.properties");
        if (!properties.exists()) {
            if (!properties.createNewFile()) {
                throw new IllegalStateException(String.format("Containers reuse was requested, but could not create file [%s] to "
                        + "allow testcontainers to reuse containers. see https://java.testcontainers.org/features/reuse/", properties));
            }
        }
        return properties;
    }
}
