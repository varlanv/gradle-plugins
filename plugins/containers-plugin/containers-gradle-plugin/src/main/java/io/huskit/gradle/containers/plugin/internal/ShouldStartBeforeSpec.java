package io.huskit.gradle.containers.plugin.internal;

import io.huskit.gradle.containers.plugin.api.ShouldStartBefore;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

public abstract class ShouldStartBeforeSpec implements ShouldStartBefore {

    private boolean isSet = false;

    public abstract Property<TaskProvider<Task>> getShouldRunBeforeTaskProvider();

    public abstract Property<Task> getShouldRunBeforeTask();

    public abstract Property<String> getShouldRunBeforeTaskName();

    @Override
    public void task(TaskProvider<Task> taskProvider) {
        checkNotAlreadySet();
        getShouldRunBeforeTaskProvider().set(taskProvider);
        isSet = true;
    }

    @Override
    public void task(String taskName) {
        checkNotAlreadySet();
        getShouldRunBeforeTaskName().set(taskName);
        isSet = true;
    }

    @Override
    public void task(Task task) {
        checkNotAlreadySet();
        getShouldRunBeforeTask().set(task);
        isSet = true;
    }

    private void checkNotAlreadySet() {
        if (isSet) {
            throw new IllegalStateException("shouldRunBefore has already been set");
        }
    }

    public boolean isSet() {
        return isSet;
    }
}
