package io.huskit.containers.http;

import io.huskit.common.Sneaky;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
public final class PipeStream {

    Supplier<Stream<String>> streamSupplier;

    public PipeStream(Reader reader, int linesCount) {
        if (reader == null || linesCount == 0) {
            streamSupplier = Stream::empty;
        } else {
            var br = new BufferedReader(reader);
            streamSupplier = () ->
                    Stream.generate(() -> {
                                try {
                                    return br.readLine();
                                } catch (Exception e) {
                                    throw Sneaky.rethrow(e);
                                }
                            })
                            .limit(linesCount)
                            .onClose(Sneaky.quiet(reader::close));
        }
    }

    public PipeStream(InputStream stream, int linesCount) {
        this(stream == null ? null : new InputStreamReader(stream, StandardCharsets.UTF_8), linesCount);
    }

    public PipeStream(Pipe pipe, int linesCount) {
        this(pipe == null ? null : Channels.newInputStream(pipe.source()), linesCount);
    }
}