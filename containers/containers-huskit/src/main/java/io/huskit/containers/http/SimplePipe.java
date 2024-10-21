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
    @NonFinal
    int writesCount;
    int size;

    @SneakyThrows
    public SimplePipe(int size) {
        this.size = size;
    }

    public int useWritesCount() {
        int wc = writesCount;
        writesCount = 0;
        return wc;
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
        writesCount++;
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
        var str = charSequence.toString();
        sink.write(str);
        sink.flush();
        writesCount += str.length();
    }

    @SneakyThrows
    public void writeLine(char[] chars) {
        init();
        sink.write(chars);
        sink.flush();
        writesCount += chars.length;
    }

    @SneakyThrows
    public void write(char ch) {
        init();
        sink.write(ch);
        sink.flush();
        writesCount++;
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