package io.huskit.containers.testcontainers.mongo;

import io.huskit.containers.model.ExistingContainer;
import io.huskit.containers.model.id.ContainerId;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.util.Optional;
import java.util.function.Supplier;

public interface TestContainersDelegate {

    <T extends GenericContainer<?>> void execInContainer(Supplier<T> container, String... command);

    <T extends GenericContainer<?>> void stop(Supplier<T> container);

    <T extends GenericContainer<?>> Integer getFirstMappedPort(Supplier<T> container);

    <T extends GenericContainer<?>> void start(T container);

    String getConnectionString(Supplier<MongoDBContainer> mongoDBContainerSupplier);

    Optional<ExistingContainer> getExistingContainer(ContainerId id);

    void remove(ExistingContainer existingContainer);

    void setReuse();
}
