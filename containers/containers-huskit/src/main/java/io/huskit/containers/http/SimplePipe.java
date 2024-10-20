package io.huskit.containers.http;

import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

import java.io.*;
import java.nio.charset.StandardCharsets;

public final class SimplePipe {

    @NonFinal
    volatile boolean isInitialized = false;
    @NonFinal
    Writer sink;
    @NonFinal
    Reader source;
    @NonFinal
    PipedInputStream sourceStream;
    @NonFinal
    PipedOutputStream sinkStream;
    int size;

    @SneakyThrows
    public SimplePipe(int size) {
        this.size = size;
    }

    public PipedInputStream sourceStream() {
        init();
        return sourceStream;
    }

    public PipedOutputStream sinkStream() {
        init();
        return sinkStream;
    }

    @SneakyThrows
    public void writeToSinkStream(int i) {
        init();
        sinkStream.write(i);
    }

    @SneakyThrows
    public void init() {
        if (!isInitialized) {
            synchronized (this) {
                if (!isInitialized) {
                    sinkStream = new PipedOutputStream();
                    sourceStream = new PipedInputStream(sinkStream, size);
                    this.sink = new OutputStreamWriter(sinkStream);
                    this.source = new InputStreamReader(sourceStream, StandardCharsets.UTF_8);
                    isInitialized = true;
                }
            }
        }
    }

    public Boolean isInitialized() {
        return isInitialized;
    }

    public Reader source() {
        init();
        return source;
    }

    @SneakyThrows
    public void writeLine(CharSequence charSequence) {
        init();
        sink.write(charSequence.toString());
        sink.flush();
    }

    @SneakyThrows
    public void write(char ch) {
        init();
        sink.write(ch);
        sink.flush();
    }

    @SneakyThrows
    public void close() {
        var sink = this.sink;
        if (sink != null) {
            sink.close();
        }
        var source = this.source;
        if (source != null) {
            source.close();
        }
    }

    @SneakyThrows
    public void breakPipe() {
        if (sink != null) {
            sink.close();
        }
    }
}