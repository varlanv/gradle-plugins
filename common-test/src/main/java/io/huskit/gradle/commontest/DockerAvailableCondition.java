package io.huskit.gradle.commontest;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.util.List;

public class DockerAvailableCondition implements ExecutionCondition {

    private static volatile Boolean dockerAvailable;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (isDockerAvailable()) {
            return ConditionEvaluationResult.enabled("Docker CLI is available.");
        } else {
            return ConditionEvaluationResult.disabled("Docker CLI is not available.");
        }
    }

    private boolean isDockerAvailable() {
        var available = dockerAvailable;
        if (available == null) {
            synchronized (DockerAvailableCondition.class) {
                available = dockerAvailable;
                if (available == null) {
                    try {
                        var process = new ProcessBuilder(List.of("docker", "--version")).start();
                        var exitCode = process.waitFor();
                        process.destroy();
                        available = exitCode == 0;
                    } catch (IOException | InterruptedException e) {
                        available = false;
                    }
                    dockerAvailable = available;
                }
            }
        }
        return available;
    }
}
