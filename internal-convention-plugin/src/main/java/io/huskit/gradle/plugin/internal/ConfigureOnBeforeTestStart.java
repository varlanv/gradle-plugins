package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

@RequiredArgsConstructor
public class ConfigureOnBeforeTestStart implements Action<Task> {

    Provider<TestSynchronizerBuildService> syncBuildService;

    @Override
    @SneakyThrows
    public void execute(Task task) {
        if (!(task instanceof Test)) {
            throw new IllegalArgumentException("Task must be of type " + Test.class.getName());
        }
        var test = (Test) task;
        test.systemProperty("io.huskit.gradle.build.sync.file.path", syncBuildService.get().syncFilePath());
//        task.getLogger().error("Running some test task with seed: [{}]", bsProvider.get().seed());
    }
}
