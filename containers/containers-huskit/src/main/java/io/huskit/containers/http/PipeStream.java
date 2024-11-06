package io.huskit.containers.http;

import io.huskit.common.Sneaky;
import lombok.Getter;

import java.io.BufferedReader;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
public final class PipeStream {

    Supplier<Stream<String>> streamSupplier;

    public PipeStream(SimplePipe simplePipe) {
        streamSupplier = () -> {
            if (!simplePipe.isInitialized()) {
                return Stream.empty();
            } else {
                var reader = simplePipe.source();
                var br = new BufferedReader(reader);
                return Stream.generate(() -> {
                        try {
                            return br.readLine();
                        } catch (Exception e) {
                            throw Sneaky.rethrow(e);
                        }
                    })
                    .takeWhile(Objects::nonNull)
                    .onClose(Sneaky.quiet(reader::close));
            }
        };
    }
}
