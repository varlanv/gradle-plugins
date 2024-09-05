package io.huskit.gradle.plugin.internal;

import org.junit.platform.launcher.LauncherInterceptor;

public class SyncTestLauncherInterceptor implements LauncherInterceptor {

    @Override
    public <T> T intercept(Invocation<T> invocation) {
        return invocation.proceed();
    }

    @Override
    public void close() {

    }
}
