package io.huskit.containers.testcontainers.mongo;

import io.huskit.containers.model.ExistingContainer;
import io.huskit.containers.model.id.ContainerKey;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Optional;
import java.util.function.Supplier;

public interface TestContainersDelegate {

    <T extends GenericContainer<?>> void execInContainer(Supplier<T> container, String... command);

    <T extends GenericContainer<?>> void stop(Supplier<T> container);

    <T extends GenericContainer<?>> Integer getFirstMappedPort(Supplier<T> container);

    <T extends GenericContainer<?>> void start(T container);

    String getConnectionString(Supplier<MongoDBContainer> mongoDbContainerSupplier);

    Optional<ExistingContainer> getExistingContainer(ContainerKey id);

    void remove(ExistingContainer existingContainer);

    void setReuse();
}
