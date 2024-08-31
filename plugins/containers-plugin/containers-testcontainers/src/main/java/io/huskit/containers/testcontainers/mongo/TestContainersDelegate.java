package io.huskit.containers.testcontainers.mongo;

import lombok.SneakyThrows;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestContainersDelegate {

    private static final AtomicBoolean initialized = new AtomicBoolean();

    @SneakyThrows
    public static void setReuse() {
        if (!initialized.get()) {
            synchronized (TestContainersDelegate.class) {
                if (!initialized.get()) {
                    var userHomePath = System.getProperty("user.home");
                    var userHome = new File(userHomePath);
                    var properties = new File(userHome, ".testcontainers.properties");
                    if (!properties.exists()) {
                        properties.createNewFile();
                    }
                    var props = new Properties();
                    try (var reader = new FileReader(properties)) {
                        props.load(reader);
                    }
                    props.put("testcontainers.reuse.enable", "true");
                    try (var writer = new FileWriter(properties)) {
                        props.store(writer, "Modified by gradle-containers-plugin");
                    }
                    initialized.set(true);
                }
            }
        }
    }
}
