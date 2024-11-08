package io.huskit.containers.api.container.logs;

import io.huskit.common.Sneaky;
import io.huskit.containers.http.PipeStream;
import io.huskit.containers.http.SimplePipe;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

public interface Logs {

    Stream<String> stdOut();

    Stream<String> stdErr();

    default Stream<String> all() {
        return Stream.concat(stdOut(), stdErr());
    }

    @Getter
    @RequiredArgsConstructor
    final class DfLogs implements Logs {

        List<String> stdOut;
        List<String> stdErr;

        @Override
        public Stream<String> stdOut() {
            return stdOut.stream();
        }

        @Override
        public Stream<String> stdErr() {
            return stdErr.stream();
        }
    }

    @Getter
    @RequiredArgsConstructor
    final class PipeLogs implements Logs {

        SimplePipe stdOutPipe;
        SimplePipe stdErrPipe;

        public void close() {
            Sneaky.tryAll(
                stdOutPipe::close,
                stdErrPipe::close
            );
        }

        @Override
        public Stream<String> stdOut() {
            return new PipeStream(stdOutPipe).streamSupplier().get();
        }

        @Override
        public Stream<String> stdErr() {
            return new PipeStream(stdErrPipe).streamSupplier().get();
        }
    }
}
