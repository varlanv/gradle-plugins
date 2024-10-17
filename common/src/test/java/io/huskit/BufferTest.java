package io.huskit;

import io.huskit.gradle.commontest.UnitTest;
import lombok.experimental.NonFinal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.*;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.CompletionHandler;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Just some tests for educational purposes for JDK {@link java.nio.Buffer} classes.
 */
public class BufferTest implements UnitTest {

    @Test
    void byte_buffer() {
        var subject = ByteBuffer.allocate(512);

        assertThat(subject.position()).isEqualTo(0);
        assertThat(subject.limit()).isEqualTo(512);
        assertThat(subject.capacity()).isEqualTo(512);

        subject.putChar('a');
        assertThat(subject.position()).isEqualTo(2);
        subject.putInt(42);
        assertThat(subject.position()).isEqualTo(6);

        subject.flip();
        assertThat(subject.position()).isEqualTo(0);
        assertThat(subject.limit()).isEqualTo(6);

        assertThat(subject.getChar()).isEqualTo('a');
        assertThat(subject.getInt()).isEqualTo(42);
        assertThat(subject.position()).isEqualTo(6);

        assertThatThrownBy(subject::getInt)
                .isInstanceOf(BufferUnderflowException.class);

        subject.clear();
        assertThat(subject.position()).isEqualTo(0);
        assertThat(subject.limit()).isEqualTo(512);

        subject.putInt('\r');
        subject.putInt('\n');
        subject.flip();

        assertThat(subject.getInt()).isEqualTo(13);
        assertThat(subject.getInt()).isEqualTo(10);
    }

    @Test
    void char_buffer() {
        var subject = CharBuffer.allocate(512);

        subject.put('a');
        assertThat(subject.position()).isEqualTo(1);
        subject.put('b');
        assertThat(subject.position()).isEqualTo(2);

        subject.flip();

        assertThat(subject.get()).isEqualTo('a');
        assertThat(subject.position()).isEqualTo(1);
        assertThat(subject.get()).isEqualTo('b');
        assertThat(subject.position()).isEqualTo(2);

        assertThatThrownBy(subject::get)
                .isInstanceOf(BufferUnderflowException.class);
    }

    @Test
    void docker_npipe_test1() throws Exception {
        try (var channel = AsynchronousFileChannel.open(
                Paths.get("\\\\.\\pipe\\docker_engine"),
                StandardOpenOption.READ,
                StandardOpenOption.WRITE)) {
            var latch = new CountDownLatch(1);
            var firstPart = "GET /containers/json HTTP/1.1\r\n".getBytes(StandardCharsets.UTF_8);
            var secondPart = "Host: localhost\r\nConnection: keep-alive\r\n\r\n".getBytes(StandardCharsets.UTF_8);
            channel.write(ByteBuffer.wrap(firstPart), 0, null, new CompletionHandler<>() {
                @Override
                public void completed(Integer result, Object attachment) {
                    channel.write(ByteBuffer.wrap(secondPart), 0, null, new CompletionHandler<>() {
                        @Override
                        public void completed(Integer result, Object attachment) {
                            var readBuffer = ByteBuffer.allocate(8192);
                            channel.read(readBuffer, 0, null, new CompletionHandler<>() {
                                @Override
                                public void completed(Integer bytesRead, Object attachment) {
                                    readBuffer.flip();
                                    var bytes = new byte[bytesRead];
                                    readBuffer.get(bytes);
                                    System.out.println(new String(bytes, StandardCharsets.UTF_8));
                                    latch.countDown();
                                }

                                @Override
                                public void failed(Throwable exc, Object attachment) {

                                }
                            });
                        }

                        @Override
                        public void failed(Throwable exc, Object attachment) {

                        }
                    });
                }

                @Override
                public void failed(Throwable exc, Object attachment) {

                }
            });

            latch.await();
        }
    }

    @Test
    void zero_copy_string_build() throws Exception {
        var post = "GET ".getBytes(StandardCharsets.UTF_8);
        var url = "/containers/json HTTP/1.1\r\n".getBytes(StandardCharsets.UTF_8);
        var host = "Hots: localhost\r\n".getBytes(StandardCharsets.UTF_8);
        var ending = "Connection: keep-alive\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        var inputStreams = new ZeroCopyInputStream(new byte[][]{post, url, host, ending});
//        var socket = new Socket("localhost", 80);
//        var outputStream = socket.getOutputStream();
//        int i;
//        while ((i = inputStreams.read()) != -1) {
//            outputStream.write(i);
//        }
//        outputStream.flush();
    }

    @Test
    @Timeout(2)
    void pipe_release_test() throws Exception {
        var pipe = Pipe.open();
        try (var sink = pipe.sink();
             var source = pipe.source()) {
            var writer = new BufferedWriter(new OutputStreamWriter(Channels.newOutputStream(sink), StandardCharsets.UTF_8));
            var reader = new BufferedReader(Channels.newReader(source, StandardCharsets.UTF_8));

            writer.write("line");
            writer.close();
            System.out.println(reader.read());
            System.out.println(reader.readLine());
            System.out.println(reader.readLine());
        }
    }

    static class ZeroCopyInputStream extends InputStream {

        byte[][] byteArrays;
        @NonFinal
        int currentArrayIndex;
        @NonFinal
        int currentArrayPosition;

        public ZeroCopyInputStream(byte[][] byteArrays) {
            if (byteArrays == null || byteArrays.length == 0) {
                throw new IllegalArgumentException("byteArrays must not be null or empty");
            }
            this.byteArrays = byteArrays;
        }

        @Override
        public int read() throws IOException {
            while (true) {
                if (currentArrayIndex >= byteArrays.length) {
                    return -1;
                }
                if (currentArrayPosition >= byteArrays[currentArrayIndex].length) {
                    currentArrayIndex++;
                    currentArrayPosition = 0;
                    continue;
                }
                return byteArrays[currentArrayIndex][currentArrayPosition++] & 0xFF;
            }
        }
    }

    //        var resultBytes = new byte[post.length + url.length + host.length + ending.length];
//        System.arraycopy(post, 0, resultBytes, 0, post.length);
//        System.arraycopy(url, 0, resultBytes, post.length, url.length);
//        System.arraycopy(host, 0, resultBytes, post.length + url.length, host.length);
//        System.arraycopy(ending, 0, resultBytes, post.length + url.length + host.length, ending.length);

//        System.out.println(new String(resultBytes, StandardCharsets.UTF_8));

//        var readableByteChannel = Channels.newChannel(System.in);

//        readableByteChannel.read(buffer)

}
