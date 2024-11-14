package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.function.ThrowingSupplier;
import io.huskit.containers.internal.HtJson;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpDockerSocketIntegrationTest implements DockerIntegrationTest {

    String dockerNpipe = "\\\\.\\pipe\\docker_engine";

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    void async_file_channel_raw_logs_follow() throws Exception {
        var containerId = "550993efd418";
        var request = "GET /containers/" + containerId + "/logs?stdout=true&stderr=true&follow=true " +
            "HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Connection: keep-alive\r\n" +
            "\r\n";
        try (var channel = AsynchronousFileChannel.open(
            Paths.get(dockerNpipe),
            StandardOpenOption.READ,
            StandardOpenOption.WRITE)) {
            channel.write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), 0).get();
            var buffer = ByteBuffer.allocate(8192);
            channel.read(buffer, 0).get();
            buffer.flip();
            var array = Arrays.copyOfRange(buffer.array(), buffer.position(), buffer.limit());
            var sb = new StringBuilder(new String(array, StandardCharsets.UTF_8));
            buffer.clear();
            channel.read(buffer, 0).get();
            buffer.flip();
            array = Arrays.copyOf(array, array.length + buffer.remaining());
            System.arraycopy(buffer.array(), buffer.position(), array, array.length - buffer.remaining(), buffer.remaining());
            sb = new StringBuilder(new String(array, StandardCharsets.UTF_8));
//            Files.write(Paths.get("logs.txt"), sb.toString().getBytes(StandardCharsets.UTF_8));
            buffer.clear();
            channel.read(buffer, 0).get();
            buffer.flip();
            array = Arrays.copyOf(array, array.length + buffer.remaining());
            System.arraycopy(buffer.array(), buffer.position(), array, array.length - buffer.remaining(), buffer.remaining());
            sb = new StringBuilder(new String(array, StandardCharsets.UTF_8));

            System.out.println();
//            Files.write(Paths.get("logs.txt"), sb.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    void test_async_blocking_with_logs_follow() throws Exception {
        var containerId = "41bca39fe688f235f3ac9cc9ce446b8f620cffc3f576b23be04b31e91cdd9d79";
        var request = "GET /containers/" + containerId + "/logs?stdout=true&stderr=true&follow=true " +
            "HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Connection: keep-alive\r\n" +
            "\r\n";
        var executor = Executors.newScheduledThreadPool(1);
        try (var channel = AsynchronousFileChannel.open(
            Paths.get(dockerNpipe),
            EnumSet.of(
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
            ),
            executor)) {
            channel.write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), 0).get();
            var buffer = ByteBuffer.allocate(8192);
            var counter = new AtomicInteger();
            executor.scheduleWithFixedDelay(
                () -> System.out.println("Counting -> " + counter.incrementAndGet()),
                0,
                1,
                TimeUnit.SECONDS
            );
            readAsync(
                channel,
                buffer
            ).thenCompose(
                bytes -> {
                    System.out.println(new String(bytes, StandardCharsets.UTF_8));
                    return readAsync(channel, buffer);
                }
            ).thenCompose(
                bytes -> {
                    System.out.println(new String(bytes, StandardCharsets.UTF_8));
                    return readAsync(channel, buffer);
                }
            ).thenCompose(
                bytes -> {
                    System.out.println(new String(bytes, StandardCharsets.UTF_8));
                    return readAsync(channel, buffer);
                }
            );
            Thread.sleep(10_000);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
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
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    void async_file_channel_raw_log_follow() throws Exception {
        var containerId = "c22b114e40ebd3de59593a11181bdddd490c886a60b20efe9be174d9b8f3d49b";
        var request = "GET " + "/containers/" + containerId + "/logs?stdout=true&stderr=true&follow=true HTTP/1.1\r\nHost: localhost\r\nConnection: keep-alive\r\n\r\n";
        try (var channel = AsynchronousFileChannel.open(
            Paths.get(dockerNpipe),
            StandardOpenOption.READ,
            StandardOpenOption.WRITE)) {
            ThrowingSupplier<String> readOnce = () -> {
                var buffer = ByteBuffer.allocate(16512);
                channel.read(buffer, 0).get();
                buffer.flip();
                return StandardCharsets.UTF_8.decode(buffer).toString();
            };
            ThrowingRunnable r = () -> {
                var time = System.currentTimeMillis();
                channel.write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), 0).get();
                var buffer = ByteBuffer.allocate(16512);
                channel.read(buffer, 0).get();
                buffer.flip();
                int i = 0;
//                readOnce.get();
//                readOnce.get();
//                System.out.println("Time: " + Duration.ofMillis(System.currentTimeMillis() - time));
                while (true) {
                    var data = readOnce.get();
                    System.out.println("ITERATING -> " + i++ + " -> " + data);
                }
            };
            r.run();
        }
    }

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    void async_file_channel_raw1() throws Exception {
        var createRequest = "POST /containers/create HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Connection: keep-alive\r\n" +
            "Content-Type: application/json\r\n" +
            "Content-Length: 141\r\n" +
            "\r\n" +
            "{\"Image\":\"public.ecr.aws/docker/library/alpine:3.20.3\",\"Cmd\":[\"sh\",\"-c\",\"echo 'Hello World 1' && echo 'Hello World 2' && tail -f /dev/null\"]}";
        var startRequest = "POST /containers/%s/start HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Connection: keep-alive\r\n" +
            "Content-Type: application/json\r\n" +
            "\r\n";
        var logsRequest = "GET /containers/%s/logs?stdout=true&stderr=true HTTP/1.1\r\n" +
            "Host: localhost\r\n" +
            "Connection: keep-alive\r\n" +
            "\r\n";
        try (var channel = AsynchronousFileChannel.open(
            Paths.get(dockerNpipe),
            StandardOpenOption.READ,
            StandardOpenOption.WRITE)) {
            ThrowingRunnable r = () -> {
                var time = System.currentTimeMillis();
                channel.write(ByteBuffer.wrap(createRequest.getBytes(StandardCharsets.UTF_8)), 0).get();
                var buffer = ByteBuffer.allocate(16536);
                channel.read(buffer, 0).get();
                buffer.flip();
                var decode1 = StandardCharsets.UTF_8.decode(buffer).toString();
                buffer.clear();
                channel.read(buffer, 0).get();
                buffer.flip();
                var decode2 = StandardCharsets.UTF_8.decode(buffer).toString();
//                System.out.println(decode1);
//                System.out.println(decode2);
                var jsonObject = new JSONObject(decode2.split("\r\n")[1]);
                var id = jsonObject.getString("Id");
                channel.write(ByteBuffer.wrap(String.format(startRequest, id).getBytes(StandardCharsets.UTF_8)), 0).get();
                buffer.clear();
                channel.read(buffer, 0).get();
                buffer.flip();
                var decode3 = StandardCharsets.UTF_8.decode(buffer).toString();
//                System.out.println(decode3);
                channel.write(ByteBuffer.wrap(String.format(logsRequest, id).getBytes(StandardCharsets.UTF_8)), 0).get();
                buffer.clear();
                channel.read(buffer, 0).get();
                buffer.flip();
                var decode4 = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println(decode4);
                buffer.clear();
                channel.read(buffer, 0).get();
                buffer.flip();
                var decode5 = StandardCharsets.UTF_8.decode(buffer).toString();
                System.out.println(decode5);
            };
            r.run();
        }
    }

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    void file_channel_raw() throws Exception {
        var request = "GET /containers/json?all=true HTTP/1.1\r\nHost: localhost\r\n\r\n";
        try (var channel = FileChannel.open(
            Paths.get(dockerNpipe),
            StandardOpenOption.READ,
            StandardOpenOption.WRITE)) {
            microBenchmark(
                5000,
                () -> {
                    channel.write(ByteBuffer.wrap(request.getBytes(StandardCharsets.UTF_8)), 0);
                    var buffer = ByteBuffer.allocate(4096);
                    channel.read(buffer, 0);
                    buffer.flip();
                    var decode = StandardCharsets.UTF_8.decode(buffer).toString();
                    var json = decode.substring(decode.indexOf("[{"));
                    var mapList = HtJson.toMapList(json);
                }
            );
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void containers_json_win() throws Exception {
        var httpRequests = new HttpRequests();
        var scheduledExecutorService = Executors.newScheduledThreadPool(1);
        var subject = new HttpDockerSocket(
            () -> HttpAsyncChannel.npipe(
                HtConstants.NPIPE_SOCKET,
                scheduledExecutorService
            ),
            scheduledExecutorService,
            Log.noop()
        );
        try {
            var actual = subject.sendPushAsync(
                    new PushRequest<>(
                        new Request(
                            httpRequests.get(
                                HtUrl.of("/containers/json?all=true")
                            )
                        ),
                        new PushJsonArray()
                    )
                )
                .join();
        } finally {
            subject.release();
        }
    }

    @Test
    void tst() throws Exception {
        try (var channel = SocketChannel.open(UnixDomainSocketAddress.of(HtConstants.UNIX_SOCKET))) {
            channel.write(
                ByteBuffer.wrap((
                    "POST /containers/create HTTP/1.1\r\n"
                        + "Host: localhost\r\n"
                        + "Connection: keep-alive\r\n"
                        + "Content-Type: application/json\r\n"
                        + "Content-Length: 383\r\n"
                        + "\r\n"
                        + "{\"HostConfig\":{\"PortBindings\":{\"80/tcp\":[{\"HostPort\":\"36217\"}],\"8080/tcp\":[{\"HostPort\":\"35379\"}]}},\"Labels\":{\"LABEL2\":\"LABELVALUE2\",\"LABEL1\":\"LABELVALUE1\"},\"Cmd\":[\"sh\",\"-c\",\"echo 'Hello World 1' && echo 'Hello World 123' && tail -f /dev/null\"],\"Env\":[\"ENV2=ENVVALUE2\",\"ENV1=ENVVALUE1\"],\"Image\":\"public.ecr.aws/docker/library/alpine:3.20.3\",\"ExposedPorts\":{\"80/tcp\":{},\"8080/tcp\":{}}}").getBytes(StandardCharsets.UTF_8))
            );
            var buffer = ByteBuffer.allocate(4096);
            channel.read(buffer);
            buffer.flip();
            var decode = StandardCharsets.UTF_8.decode(buffer).toString();
            System.out.println(decode);
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void containers_json_unix() throws Exception {
        var httpRequests = new HttpRequests();
        var scheduledExecutorService = Executors.newScheduledThreadPool(2);
        var subject = new HttpDockerSocket(
            () -> HttpAsyncChannel.unixDomainSocket(
                HtConstants.UNIX_SOCKET,
                scheduledExecutorService
            ),
            scheduledExecutorService,
            Log.fakeVerbose()
        );
        try {
            var actual = subject.sendPushAsync(
                    new PushRequest<>(
                        new Request(
                            httpRequests.get(
                                HtUrl.of("/containers/json?all=true")
                            )
                        ),
                        new PushJsonArray()
                    )
                )
                .join();
        } finally {
            subject.release();
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void containers_json_many() throws Exception {
        var httpRequests = new HttpRequests();
        var scheduledExecutorService = Executors.newScheduledThreadPool(1);
        var subject = new HttpDockerSocket(
            () -> HttpAsyncChannel.npipe(
                HtConstants.NPIPE_SOCKET,
                scheduledExecutorService
            ),
            scheduledExecutorService,
            Log.noop()
        );
        try {
            var count = 10;
            var latch = new CountDownLatch(count);
            for (var i = 0; i < count; i++) {
                ForkJoinPool.commonPool().submit(() -> {
                    var actual = subject.sendPushAsync(
                            new PushRequest<>(
                                new Request(
                                    httpRequests.get(
                                        HtUrl.of("/containers/json?all=true")
                                    )
                                ),
                                new PushJsonArray()
                            )
                        )
                        .join();
                    latch.countDown();
                });
            }
            latch.await();
        } finally {
            subject.release();
        }
    }

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    void containers_logs() throws Exception {
        var containerId = "82d6ca1f9bef9cbd84db417ccdec0045ec95121e13b994ee47f85dbc97b359ad";
        var httpRequests = new HttpRequests();
        var scheduledExecutorService = Executors.newScheduledThreadPool(1);
        var subject = new HttpDockerSocket(
            () -> HttpAsyncChannel.npipe(
                HtConstants.NPIPE_SOCKET,
                scheduledExecutorService
            ),
            scheduledExecutorService,
            Log.noop()
        );
        try {
            var actual = subject.sendPushAsync(
                    new PushRequest<>(
                        new Request(
                            httpRequests.get(
                                HtUrl.of(String.format("/containers/%s/logs?stdout=true&stderr=true", containerId))
                            )
                        ),
                        new PushMultiplexedStream()
                    )
                )
                .join();
        } finally {
            subject.release();
        }
    }

    private CompletableFuture<byte[]> readAsync(AsynchronousFileChannel channel, ByteBuffer byteBuffer) {
        var completion = new CompletableFuture<byte[]>();
        channel.read(byteBuffer, 0, null, new CompletionHandler<>() {

            @Override
            public void completed(Integer result, Object attachment) {
                byteBuffer.flip();
                var bytes = new byte[byteBuffer.remaining()];
                System.arraycopy(byteBuffer.array(), byteBuffer.position(), bytes, 0, byteBuffer.remaining());
                completion.complete(bytes);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                completion.completeExceptionally(exc);
            }
        });
        return completion;
    }
}
