package io.huskit.containers.testcontainers.mongo;

import lombok.SneakyThrows;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.io.Serializable;
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
    public <T extends GenericContainer<?>> void start(Supplier<T> container) {
        container.get().start();
    }

    @Override
    public String getConnectionString(Supplier<MongoDBContainer> mongoDBContainerSupplier) {
        return mongoDBContainerSupplier.get().getConnectionString();
    }
}
