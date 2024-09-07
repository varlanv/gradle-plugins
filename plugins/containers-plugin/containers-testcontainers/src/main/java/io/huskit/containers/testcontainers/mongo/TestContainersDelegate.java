package io.huskit.containers.testcontainers.mongo;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.util.function.Supplier;

public interface TestContainersDelegate {

    <T extends GenericContainer<?>> void execInContainer(Supplier<T> container, String... command);

    <T extends GenericContainer<?>> void stop(Supplier<T> container);

    <T extends GenericContainer<?>> int getFirstMappedPort(Supplier<T> container);

    <T extends GenericContainer<?>> void start(Supplier<T> container);

    String getConnectionString(Supplier<MongoDBContainer> mongoDBContainerSupplier);
}
