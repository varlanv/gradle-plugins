package io.huskit.containers.api.container.logs;

import io.huskit.common.Sneaky;
import io.huskit.containers.http.PipeStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.Reader;
import java.util.stream.Stream;

public interface Logs extends AutoCloseable {

    Stream<String> stdOut();

    Stream<String> stdErr();

    default Stream<String> all() {
        return Stream.concat(stdOut(), stdErr());
    }

    @Getter
    @RequiredArgsConstructor
    class DfLogs implements Logs {

        Stream<String> stdOut;
        Stream<String> stdErr;

        public DfLogs(Reader stdOutReader, Reader stdErrReader) {
            stdOut = new PipeStream(stdOutReader, 0).streamSupplier().get();
            stdErr = new PipeStream(stdErrReader, 0).streamSupplier().get();
        }

        @Override
        public void close() throws IOException {
            Sneaky.doTry(
                    stdOut::close,
                    stdErr::close
            );
        }
    }
}
