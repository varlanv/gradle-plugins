package io.huskit.gradle;

import io.huskit.gradle.commontest.BaseTest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SynchronizingTestListener implements TestExecutionListener {

    private static final String SYNC_FILE_PATH_STR = System.getProperty("io.huskit.gradle.build.sync.file.path");
    private static final TestTag DOCKER_TEST_TAG_OBJ = TestTag.create(BaseTest.DOCKER_TEST_TAG);
    private static final File SYNC_FILE;
    private final ConcurrentMap<String, LockHolder> syncMap = new ConcurrentHashMap<>();

    static {
        if (SYNC_FILE_PATH_STR != null && !SYNC_FILE_PATH_STR.isEmpty()) {
            SYNC_FILE = new File(SYNC_FILE_PATH_STR);
        } else {
            SYNC_FILE = null;
        }
    }

    @Override
    @SneakyThrows
    public void executionStarted(TestIdentifier testIdentifier) {
        if (SYNC_FILE != null && testIdentifier.isTest() && testIdentifier.getTags().contains(DOCKER_TEST_TAG_OBJ)) {
            var raf = new RandomAccessFile(SYNC_FILE, "rw");
            var channel = raf.getChannel();
            var lock = channel.lock();
            syncMap.put(testIdentifier.getUniqueId(), new LockHolder(raf, channel, lock));
        }
    }

    @Override
    @SneakyThrows
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (SYNC_FILE != null && testIdentifier.isTest()) {
            var uniqueId = testIdentifier.getUniqueId();
            var lockHolder = syncMap.get(uniqueId);
            if (lockHolder != null) {
                try (var raf = lockHolder.fis;
                     var channel = lockHolder.channel;
                     var lock = lockHolder.lock) {
                    lock.release();
                } finally {
                    syncMap.remove(uniqueId);
                }
            }
        }
    }

    @RequiredArgsConstructor
    private static class LockHolder {

        RandomAccessFile fis;
        FileChannel channel;
        FileLock lock;
    }
}
