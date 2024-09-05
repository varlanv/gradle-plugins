package io.huskit.gradle.commontest;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

class TestUtils {

    private static final AtomicReference<File> huskitProjectRoot = new AtomicReference<>();

    static File huskitProjectRoot() {
        return huskitProjectRoot.get();
    }

    static void setHuskitProjectRoot(Supplier<File> fileSupplier) {
        if (huskitProjectRoot.get() == null) {
            synchronized (TestUtils.class) {
                huskitProjectRoot.set(fileSupplier.get());
            }
        }
    }
}