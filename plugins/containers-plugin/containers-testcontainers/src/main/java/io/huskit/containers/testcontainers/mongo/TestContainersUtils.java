package io.huskit.containers.testcontainers.mongo;

import com.github.dockerjava.api.model.Container;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.testcontainers.DockerClientFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class TestContainersUtils {

    AtomicBoolean initialized = new AtomicBoolean();
    Log log;

    /**
     * Enables reuse of TestContainers containers.
     * Testcontainers library requires property {@code testcontainers.reuse.enable} to be set to {@code true} in
     * {@code ~/.testcontainers.properties} file.
     */
    @SneakyThrows
    public void setReuse() {
        if (!initialized.get()) {
            synchronized (this) {
                if (!initialized.get()) {
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
                        initialized.set(true);
                    }
                }
            }
        }
    }

    public List<Map<String, String>> findHuskitContainers() {
        var client = DockerClientFactory.instance().client();
        var listContainersCmd = client.listContainersCmd().withLabelFilter(Map.of("huskit_container", "true"));
        return listContainersCmd.exec().stream()
                .map(Container::getLabels)
                .collect(Collectors.toList());
    }


    private File getTestcontainerPropertiesFile(String userHomePath) throws IOException {
        var userHome = new File(userHomePath);
        var properties = new File(userHome, ".testcontainers.properties");
        if (!properties.exists()) {
            if (!properties.createNewFile()) {
                throw new IllegalStateException(String.format("Containers reuse was requested, but could not create file [%s] to" +
                        " allow testcontainers to reuse containers. see https://java.testcontainers.org/features/reuse/", properties));
            }
        }
        return properties;
    }
}
