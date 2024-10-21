package io.huskit.containers.http;

import io.huskit.common.FakeTestLog;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

class NpipeChannelInTest implements UnitTest {

    @Test
    void asd(@TempDir Path path) throws Exception {
        var someFile = Files.createFile(path.resolve("test"));
        var log = new FakeTestLog();
        var lock = new NpipeChannelLock(log);
        var isDirtyConnection = new AtomicBoolean();
        try (var channel = AsynchronousFileChannel.open(someFile)) {
            var subject = new NpipeChannelIn(channel, isDirtyConnection, lock);
//            subject.takeLock();
        }
    }
}
