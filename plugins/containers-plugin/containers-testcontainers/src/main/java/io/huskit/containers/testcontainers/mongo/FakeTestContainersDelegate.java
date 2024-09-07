package io.huskit.containers.testcontainers.mongo;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;

import java.io.Serializable;
import java.util.function.Supplier;

public class FakeTestContainersDelegate implements TestContainersDelegate, Serializable {

    @Override
    public <T extends GenericContainer<?>> void execInContainer(Supplier<T> container, String... command) {

    }

    @Override
    public <T extends GenericContainer<?>> void stop(Supplier<T> container) {

    }

    @Override
    public <T extends GenericContainer<?>> int getFirstMappedPort(Supplier<T> container) {
        return 0;
    }

    @Override
    public <T extends GenericContainer<?>> void start(Supplier<T> container) {

    }

    @Override
    public String getConnectionString(Supplier<MongoDBContainer> mongoDBContainerSupplier) {
        return "";
    }
}
