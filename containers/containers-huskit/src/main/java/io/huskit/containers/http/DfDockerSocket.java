package io.huskit.containers.http;

import io.huskit.common.function.MemoizedSupplier;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

class DfDockerSocket implements DockerSocket {

    MemoizedSupplier<OpenedSocket> socket;

    public DfDockerSocket(Supplier<Socket> socket) {
        this.socket = MemoizedSupplier.of(() -> new OpenedSocket(socket.get()));
    }

    @Override
    public CompletableFuture<DockerResponse> sendAsync(DockerRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }

    @Override
    @Synchronized
    @SneakyThrows
    public DockerResponse send(DockerRequest request) {
        var openedSocket = socket.get();
        var writer = openedSocket.writer();
        writer.write(request.body());
        writer.flush();

        var is = openedSocket.socket().getInputStream();
        var headers = new HashMap<String, String>();
        int status = -1;
        int i;
        var headerLineCount = 0;
        var charBuffer = CharBuffer.allocate(1024);
        while (true) {
            i = is.read();
            var ch = (char) i;
            if (ch == '\n') {
                break;
            }
            charBuffer.put(ch);
//            if (headerLineCount == 0) {
//                var split = headerLine.split(" ");
//                status = Integer.parseInt(split[1]);
//            } else {
//                var split = headerLine.split(": ");
//                headers.put(split[0], split[1]);
//            }
//            headerLineCount++;
        }
        System.out.println();

        var transferEncoding = headers.get("Transfer-Encoding");
        var isChunked = transferEncoding != null && transferEncoding.equals("chunked");
        var lines = new ArrayList<String>();

        String bodyLine = "";
        String prevBodyLine = null;
        var bodyLineCount = 0;
        if (status != 204) {
//            while ((bodyLine = is.readLine()) != null) {
            while (true) {
                if (bodyLineCount == 0 && isChunked) {
                    bodyLineCount++;
                    continue;
                }
                if (isChunked && prevBodyLine != null && prevBodyLine.equals("0")) {
                    break;
                }
                if (!bodyLine.isEmpty() && !(bodyLine.equals("0") && (prevBodyLine == null || prevBodyLine.isEmpty()))) {
                    lines.add(bodyLine);
                }
                prevBodyLine = bodyLine;
                bodyLineCount++;
            }
        }
        return new DfDockerResponse(
                new DfHead(
                        status,
                        Collections.unmodifiableMap(headers)
                ),
                new DfBody(
                        Collections.unmodifiableList(lines)
                )
        );
    }

    @Override
    @SneakyThrows
    public void close() {
        if (socket.isInitialized()) {
            var openedSocket = socket.get();
            openedSocket.reader().close();
            openedSocket.writer().close();
            openedSocket.socket().close();
        }
    }

    @Getter
    private static final class OpenedSocket {

        Socket socket;
        BufferedReader reader;
        OutputStream writer;

        @SneakyThrows
        private OpenedSocket(Socket socket) {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = this.socket.getOutputStream();
        }
    }
}
