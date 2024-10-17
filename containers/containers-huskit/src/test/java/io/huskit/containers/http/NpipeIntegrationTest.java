package io.huskit.containers.http;

import io.huskit.gradle.commontest.DockerIntegrationTest;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@EnabledOnOs(OS.WINDOWS)
public class NpipeIntegrationTest implements DockerIntegrationTest {

    String dockerNpipe = "\\\\.\\pipe\\docker_engine";

    @BeforeAll
    void setupAll() throws Exception {
        for (int i = 0; i < 10; i++) {
            ForkJoinPool.commonPool().submit(() -> "").get();
        }
    }

    @Test
    void async_file_channel_raw() throws Exception {
        var request = "GET /containers/json?all=true HTTP/1.1\r\nHost: localhost\r\n\r\n";
        try (var channel = AsynchronousFileChannel.open(
                Paths.get(dockerNpipe),
                StandardOpenOption.READ,
                StandardOpenOption.WRITE)) {
            ThrowingRunnable r = () -> {
                var time = System.currentTimeMillis();
                channel.write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), 0).get();
                var buffer = ByteBuffer.allocate(4096);
                channel.read(buffer, 0).get();
                buffer.flip();
                var decode = StandardCharsets.UTF_8.decode(buffer).toString();
                var lines = Arrays.asList(decode.split("\r\n"));
                var headers = new HashMap<String, String>();
                for (int i = 0; i < lines.size(); i++) {
                    if (i == 0) {
                        continue;
                    }
                    var line = lines.get(i);
                    String[] split = line.split(":");
                    if (split.length == 2) {
                        headers.put(split[0].trim(), split[1].trim());
                    }
                    if (line.isEmpty()) {
                        break;
                    }
                }
                var list = new JSONArray(lines.get(lines.size() - 1)).toList();
                System.out.println(list);
                System.out.println("Time: " + Duration.ofMillis(System.currentTimeMillis() - time));
            };
            r.run();
            r.run();
        }
    }

    @Test
    void file_channel_raw() throws Exception {
        var request = "GET /containers/json?all=true HTTP/1.1\r\nHost: localhost\r\n\r\n";
        try (var channel = FileChannel.open(
                Paths.get(dockerNpipe),
                StandardOpenOption.READ,
                StandardOpenOption.WRITE)) {
            channel.write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), 0);
            var buffer = ByteBuffer.allocate(4096);
            channel.read(buffer, 0);
            buffer.flip();
            var decode = StandardCharsets.UTF_8.decode(buffer).toString();
            System.out.println(decode);
        }
    }

    @RequiredArgsConstructor
    static class NpipeSocketAddress extends SocketAddress {

        String path;
    }

    @Test
    void b() throws Exception {
//        AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(1)).provider()
        var request = "GET /containers/json?all=true HTTP/1.1\r\nHost: localhost\r\n\r\n";
        var es = Executors.newFixedThreadPool(1);
        var asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(es);
        var latch = new CountDownLatch(1);
    }

    @Test
    @Timeout(3)
    void containers_json() throws Exception {
        var httpRequests = new HttpRequests();
        var subject = new Npipe(dockerNpipe);
        try {
            ThrowingRunnable r = () -> {
                long before = System.currentTimeMillis();
                subject.sendAsync(
                                httpRequests.generate(
                                        HttpMethod.GET,
                                        "/containers/json?all=true"
                                ).emptyBody(),
                                httpFlow -> new JSONArray(
                                        new JSONTokener(
                                                httpFlow.reader()
                                        )
                                ).toList()
                        )
                        .thenAccept(it -> System.out.println(it.body().list()))
                        .join();
                System.out.println("Time: " + Duration.ofMillis(System.currentTimeMillis() - before));
            };
            r.run();
            r.run();
        } finally {
            subject.close();
        }
    }
}
