package io.huskit.containers.http.npipe;

import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class NamedPipeSocket extends Socket {

    String socketFileName;

    @NonFinal
    AsynchronousFileByteChannel channel;

    public NamedPipeSocket(String socketFileName) {
        this.socketFileName = socketFileName;
    }

    public NamedPipeSocket() {
        this("\\\\.\\pipe\\docker_engine");
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    @Override
    @SneakyThrows
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        long startedAt = System.currentTimeMillis();
        timeout = Math.max(timeout, 10_000);
        while (true) {
            try {
                channel = new AsynchronousFileByteChannel(
                        AsynchronousFileChannel.open(
                                Paths.get(socketFileName),
                                StandardOpenOption.READ,
                                StandardOpenOption.WRITE
                        )
                );
                break;
            } catch (FileSystemException e) {
                if (System.currentTimeMillis() - startedAt >= timeout) {
                    throw new RuntimeException(e);
                } else {
                    Thread.sleep(1000);
                }
            }
        }
    }

    @Override
    public InputStream getInputStream() {
        return Channels.newInputStream(channel);
    }

    @Override
    public OutputStream getOutputStream() {
        return Channels.newOutputStream(channel);
    }

    private static class AsynchronousFileByteChannel implements AsynchronousByteChannel {
        private final AsynchronousFileChannel fileChannel;

        AsynchronousFileByteChannel(AsynchronousFileChannel fileChannel) {
            this.fileChannel = fileChannel;
        }

        @Override
        public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
            fileChannel.read(dst, 0, attachment, new CompletionHandler<Integer, A>() {
                @Override
                public void completed(Integer read, A attachment) {
                    handler.completed(read > 0 ? read : -1, attachment);
                }

                @Override
                public void failed(Throwable exc, A attachment) {
                    if (exc instanceof AsynchronousCloseException) {
                        handler.completed(-1, attachment);
                        return;
                    }
                    handler.failed(exc, attachment);
                }
            });
        }

        @Override
        public Future<Integer> read(ByteBuffer dst) {
            CompletableFutureHandler future = new CompletableFutureHandler();
            fileChannel.read(dst, 0, null, future);
            return future;
        }

        @Override
        public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
            fileChannel.write(src, 0, attachment, handler);
        }

        @Override
        public Future<Integer> write(ByteBuffer src) {
            return fileChannel.write(src, 0);
        }

        @Override
        public void close() throws IOException {
            fileChannel.close();
        }

        @Override
        public boolean isOpen() {
            return fileChannel.isOpen();
        }

        private static final class CompletableFutureHandler extends CompletableFuture<Integer> implements CompletionHandler<Integer, Object> {

            @Override
            public void completed(Integer read, Object attachment) {
                complete(read > 0 ? read : -1);
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                if (exc instanceof AsynchronousCloseException) {
                    complete(-1);
                    return;
                }
                completeExceptionally(exc);
            }
        }
    }
}