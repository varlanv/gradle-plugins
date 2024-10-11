package io.huskit.gradle;

import lombok.SneakyThrows;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProfilingTestListener implements TestExecutionListener {

    private final ConcurrentMap<String, Long> syncMap = new ConcurrentHashMap<>();

    @Override
    @SneakyThrows
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            syncMap.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
        }
    }

    @Override
    @SneakyThrows
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            var uniqueId = testIdentifier.getUniqueId();
            var millis = syncMap.get(uniqueId);
            if (millis != null) {
                System.out.printf("Test '%s' finished in '%s'\n", uniqueId, Duration.ofMillis(System.currentTimeMillis() - millis));
                syncMap.remove(uniqueId);
            }
        }
    }
}
