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
            var millisBefore = syncMap.get(uniqueId);
            if (millisBefore != null) {
                var time = System.currentTimeMillis() - millisBefore;
                System.out.printf("Test '%s' finished in '%s'\n", uniqueId, time > 1000 ? Duration.ofMillis(time) : time + "ms");
                syncMap.remove(uniqueId);
            }
        }
    }
}
