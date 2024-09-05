package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

public interface ShouldStartBeforeSpec {

    void task(TaskProvider<Task> taskProvider);

    void task(String taskName);

    void task(Task task);
}
