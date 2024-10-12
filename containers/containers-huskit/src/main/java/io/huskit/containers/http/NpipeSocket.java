package io.huskit.containers.http;

import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

final class NpipeSocket extends Socket {

    private static final int DEFAULT_TIMEOUT = (int) Duration.ofSeconds(10).toMillis();
    String socketFileName;
    @NonFinal
    volatile FileByteChannel channel;

    public NpipeSocket(String socketFileName) {
        this.socketFileName = socketFileName;
        connect(null);
    }

    public NpipeSocket() {
        this("\\\\.\\pipe\\docker_engine");
    }

    @Override
    public void close() throws IOException {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    public void connect(SocketAddress endpoint) {
        connect(endpoint, DEFAULT_TIMEOUT);
    }

    @Override
    @SneakyThrows
    public void connect(SocketAddress endpoint, int timeout) {
        var before = System.currentTimeMillis();
        while (true) {
            try {
                channel = new FileByteChannel(
                        FileChannel.open(
                                Paths.get(socketFileName),
                                StandardOpenOption.READ,
                                StandardOpenOption.WRITE
                        )
                );
                break;
            } catch (FileSystemException e) {
                if (System.currentTimeMillis() - before >= timeout) {
                    throw new IllegalStateException(String.format("Failed to open name pipe withing timeout of %sms", timeout), e);
                } else {
                    Thread.sleep(50);
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

    private static class FileByteChannel implements ByteChannel {

        private final FileChannel fileChannel;

        FileByteChannel(FileChannel fileChannel) {
            this.fileChannel = fileChannel;
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            return fileChannel.read(dst);
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return fileChannel.write(src);
        }

        @Override
        public boolean isOpen() {
            return fileChannel.isOpen();
        }

        @Override
        public void close() throws IOException {
            fileChannel.close();
        }
    }
}
