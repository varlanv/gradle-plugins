package io.huskit.containers.http;

import io.huskit.common.Volatile;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.HtConstants;
import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

final class DockerNpipe implements DockerSocket {

    MemoizedSupplier<State> stateSupplier;

    DockerNpipe(String socketFile) {
        this.stateSupplier = MemoizedSupplier.of(() -> new State(socketFile));
    }

    DockerNpipe() {
        this(HtConstants.NPIPE_SOCKET);
    }


    public CompletableFuture<DockerResponse> sendAsync(DockerRequest request) {
        var responseFuture = new CompletableFuture<DockerResponse>();
        var state = stateSupplier.get();
        var channel = state.channel;

        // Write the request without specifying a position
        channel.write(ByteBuffer.wrap(request.body()), 0, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Object attachment) {
                // Prepare to read the response
                readResponse(channel, responseFuture, state.decoder);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                responseFuture.completeExceptionally(exc);
            }
        });
        return responseFuture;
    }

    private void readResponse(AsynchronousFileChannel channel,
                              CompletableFuture<DockerResponse> responseFuture,
                              CharsetDecoder decoder) {
        var readBuffer = ByteBuffer.allocate(8192); // Use a larger buffer
        var lines = new ArrayList<String>();

        // Read loop
        channel.read(readBuffer, 0, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer bytesRead, Object attachment) {
                readBuffer.flip();
                try {
                    lines.addAll(Arrays.asList(decoder.decode(readBuffer).toString().split(System.lineSeparator())));
                } catch (CharacterCodingException e) {
                    responseFuture.completeExceptionally(e);
                    return;
                }
                readBuffer.clear();
//                if (lines.get(0).contains("204") || lines.get().endsWith("0" + System.lineSeparator() + System.lineSeparator())) {
                if (lines.get(0).contains("204") || lines.get(lines.size() - 1).equals("0")) {
                    processResponse(lines, responseFuture);
                } else {
                    channel.read(readBuffer, 0, null, this);
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                responseFuture.completeExceptionally(exc);
            }
        });
    }

    @SneakyThrows
    private void processResponse(List<String> lines, CompletableFuture<DockerResponse> responseFuture) {
        var headers = new HashMap<String, String>();
        var status = -1;
        var responseLines = new ArrayList<String>();
        var isHeader = true;
        var bodyLineCount = 0;
        for (var idx = 0; idx < lines.size(); idx++) {
            var line = lines.get(idx);
            var lineEmpty = line.isEmpty();
            if (lineEmpty) {
                isHeader = false;
                continue;
            }
            var te = headers.get("Transfer-Encoding");
            if (te != null && te.equals("chunked")) {
                if (idx > 0 && isHeader && !line.contains(":")) {
                    isHeader = false;
                    continue;
                }
            }
            if (isHeader) {
                if (idx == 0) {
                    var split = line.split(" ");
                    status = Integer.parseInt(split[1]);
                } else {
                    var split = line.split(": ");
                    headers.put(split[0], split[1]);
                }
            } else {
                if (status == 204) {
                    break;
                }
                if (++bodyLineCount > 1 && line.equals("0")) {
                    break;
                }
                responseLines.add(line);
            }
        }
        var dfDockerResponse = new DfDockerResponse(
                new DfHead(
                        status,
                        headers
                ),
                new DfBody(
                        responseLines
                )
        );
        responseFuture.complete(dfDockerResponse);
    }


//    @Override
//    public CompletableFuture<DockerResponse> sendAsync(DockerRequest request) {
//        var responseFuture = new CompletableFuture<DockerResponse>();
//        var state = stateSupplier.get();
//        var buffer = state.buffer;
//        buffer.clear();
//        buffer.put(request.body());
//        buffer.flip();
//        var channel = state.channel;
//        channel.write(buffer, 0, null, new CompletionHandler<>() {
//            @Override
//            public void completed(Integer result, Object attachment) {
//                buffer.clear();
//                channel.read(buffer, 0, null, new CompletionHandler<>() {
//                    @Override
//                    @SneakyThrows
//                    public void completed(Integer result, Object attachment) {
//                        buffer.flip();
//                        var decoder = state.decoder;
//                        var responseBody = decoder.decode(buffer).toString();
//                        var lines = responseBody.split(System.lineSeparator());
//                        var headers = new HashMap<String, String>();
//                        var status = -1;
//                        var responseLines = new ArrayList<String>();
//                        var isHeader = true;
//                        for (var idx = 0; idx < lines.length; idx++) {
//                            var line = lines[idx];
//                            var lineEmpty = line.isEmpty();
//                            if (lineEmpty) {
//                                isHeader = false;
//                                continue;
//                            }
//                            if (isHeader) {
//                                if (idx == 0) {
//                                    var split = line.split(" ");
//                                    status = Integer.parseInt(split[1]);
//                                } else {
//                                    var split = line.split(": ");
//                                    headers.put(split[0], split[1]);
//                                }
//                            } else {
//                                responseLines.add(line);
//                            }
//                        }
//                        var dfDockerResponse = new DfDockerResponse(
//                                new DfHead(
//                                        status,
//                                        headers
//                                ),
//                                new DfBody(
//                                        responseLines
//                                )
//                        );
//                        responseFuture.complete(dfDockerResponse);
//                    }
//
//                    @Override
//                    @SneakyThrows
//                    public void failed(Throwable exc, Object attachment) {
//                        channel.close();
//                    }
//                });
//            }
//
//            @Override
//            @SneakyThrows
//            public void failed(Throwable exc, Object attachment) {
//                channel.close();
//            }
//        });
//        return responseFuture;
//    }

    @Override
    public DockerResponse send(DockerRequest request) {
        return sendAsync(request).join();
    }

    @Override
    @SneakyThrows
    public void close() {
        if (stateSupplier.isInitialized()) {
            stateSupplier.get().channel.close();
        }
    }

    @Getter
    private static class State {

        ByteBuffer buffer;
        AsynchronousFileChannel channel;
        CharsetDecoder decoder;
        Volatile<List<String>> lines;


        @SneakyThrows
        private State(String socketFile) {
            this.buffer = ByteBuffer.allocate(2048);
            this.channel = AsynchronousFileChannel.open(
                    Paths.get(socketFile),
                    StandardOpenOption.READ,
                    StandardOpenOption.WRITE
            );
            this.decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
            this.lines = Volatile.of(new ArrayList<>());
        }
    }
}
