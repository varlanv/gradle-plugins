package io.huskit.containers.api.container.logs;

import io.huskit.common.Sneaky;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
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

        @Override
        public void close() throws IOException {
            Sneaky.doTry(
                    stdOut::close,
                    stdErr::close
            );
        }
    }
}
