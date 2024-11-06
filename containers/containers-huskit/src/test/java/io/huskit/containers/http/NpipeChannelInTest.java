//package io.huskit.containers.http;
//
//import io.huskit.common.FakeTestLog;
//import io.huskit.gradle.commontest.UnitTest;
//import lombok.experimental.NonFinal;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.io.TempDir;
//
//import java.nio.ByteBuffer;
//import java.nio.channels.AsynchronousFileChannel;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardOpenOption;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class NpipeChannelInTest implements UnitTest {
//
//    @NonFinal
//    Path someFile;
//    @NonFinal
//    FakeTestLog log;
//    @NonFinal
//    NpipeChannelLock lock;
//    @NonFinal
//    AtomicBoolean isDirtyConnection;
//    @NonFinal
//    AsynchronousFileChannel channel;
//    @NonFinal
//    NpipeChannelIn subject;
//
//    @BeforeEach
//    void setup(@TempDir Path dir) throws Exception {
//        someFile = Files.createFile(dir.resolve("test"));
//        log = new FakeTestLog();
//        lock = new NpipeChannelLock(log);
//        isDirtyConnection = new AtomicBoolean();
//        channel = AsynchronousFileChannel.open(
//                someFile,
//                StandardOpenOption.READ,
//                StandardOpenOption.WRITE
//        );
//        subject = new NpipeChannelIn(channel, isDirtyConnection, lock);
//    }
//
//    @AfterEach
//    void cleanup() throws Exception {
//        channel.close();
//    }
//
//    @Test
//    void write__when_empty_body__then_throw_exception() {
//        subject.write(new Request(Http.Request.empty()))
//                .handle((response, throwable) -> {
//                    assertThat(throwable).isInstanceOf(IllegalArgumentException.class);
//                    assertThat(throwable).hasMessageContaining("empty body");
//                    return null;
//                })
//                .join();
//    }
//
//    @Test
//    void write__when_empty_body__then_dont_lock() {
//        subject.write(new Request(Http.Request.empty()))
//                .handle((response, throwable) -> null)
//                .join();
//        assertThat(lock.isAcquired()).isFalse();
//        assertThat(log.debugMessages()).isEmpty();
//    }
//
//    @Test
//    void write__when_body_not_empty__then_result_correct() {
//        var request = new Request(
//                new DfHttpRequest(
//                        "Hello".getBytes(StandardCharsets.UTF_8)
//                )
//        );
//        var result = subject.write(request).join();
//        assertThat(result).isEqualTo(5);
//    }
//
//    @Test
//    void write__when_body_not_empty__then_lock() {
//        var request = new Request(
//                new DfHttpRequest(
//                        "Hello".getBytes(StandardCharsets.UTF_8)
//                )
//        );
//        subject.write(request).join();
//        assertThat(lock.isAcquired()).isTrue();
//        assertThat(log.debugMessages()).hasSize(1);
//        assertThat(log.debugMessages().get(0)).contains("lock").contains("Hello");
//    }
//
//    @Test
//    void write__when_body_not_empty__then_file_contains_data() throws Exception {
//        var request = new Request(
//                new DfHttpRequest(
//                        "Hello".getBytes(StandardCharsets.UTF_8)
//                )
//        );
//        subject.write(request).join();
//        var buffer = ByteBuffer.allocate(6);
//        var result = channel.read(buffer, 0).get();
//        assertThat(result).isEqualTo(5);
//        assertThat(buffer.array()).containsExactly(
//                (byte) 'H',
//                (byte) 'e',
//                (byte) 'l',
//                (byte) 'l',
//                (byte) 'o',
//                (byte) 0
//        );
//    }
//}
