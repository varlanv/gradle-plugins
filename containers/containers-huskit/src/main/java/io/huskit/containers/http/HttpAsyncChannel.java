package io.huskit.containers.http;

import lombok.SneakyThrows;

import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;

public interface HttpAsyncChannel extends AutoCloseable {

    <A> void write(ByteBuffer src,
                   long position,
                   A attachment,
                   CompletionHandler<Integer, ? super A> handler);

    <A> void read(ByteBuffer dst,
                  long position,
                  A attachment,
                  CompletionHandler<Integer, ? super A> handler);

    @SneakyThrows
    static HttpAsyncChannel unixDomainSocket(String socketFile, ExecutorService executor) {
        var delegate = SocketChannel.open(UnixDomainSocketAddress.of(socketFile));
        return new HttpAsyncChannel() {

            @Override
            public <A> void write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
                try {
                    delegate.write(src);
                    handler.completed(src.position(), attachment);
                } catch (Exception e) {
                    handler.failed(e, attachment);
                }
            }

            @Override
            public <A> void read(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
                try {
                    delegate.read(dst);
                    handler.completed(dst.position(), attachment);
                } catch (Exception e) {
                    handler.failed(e, attachment);
                }
            }

            @Override
            public void close() throws Exception {
                delegate.close();
            }
        };
    }

    @SneakyThrows
    static HttpAsyncChannel npipe(String socketFile, ExecutorService executor) {
        var delegate = AsynchronousFileChannel.open(
            Paths.get(socketFile),
            EnumSet.of(
                StandardOpenOption.READ,
                StandardOpenOption.WRITE
            ),
            executor
        );

        return new HttpAsyncChannel() {

            @Override
            public <A> void write(ByteBuffer src, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
                delegate.write(src, position, attachment, handler);
            }

            @Override
            public <A> void read(ByteBuffer dst, long position, A attachment, CompletionHandler<Integer, ? super A> handler) {
                delegate.read(dst, position, attachment, handler);
            }

            @Override
            public void close() throws Exception {
                delegate.close();
            }
        };
    }
}
