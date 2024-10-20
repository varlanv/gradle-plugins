package io.huskit.containers.http;

import io.huskit.common.Mutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface Http {

    interface Request {

        byte[] body();

        static Request empty() {
            return () -> new byte[0];
        }
    }

    interface ResponseStream extends AutoCloseable {

        Head head();

        Reader reader();

        @Override
        default void close() throws Exception {
            reader().close();
        }
    }

    interface Response<T> {

        Head head();

        Body<T> body();
    }

    interface RawResponse {

        Head head();

        Optional<Reader> bodyReader();

        Optional<Reader> stdOutReader();

        Optional<Reader> stdErrReader();

        @RequiredArgsConstructor
        class BodyRawResponse implements RawResponse {

            @Getter
            Head head;
            Reader bodyReader;

            @Override
            public Optional<Reader> bodyReader() {
                return Optional.of(bodyReader);
            }

            @Override
            public Optional<Reader> stdOutReader() {
                return Optional.empty();
            }

            @Override
            public Optional<Reader> stdErrReader() {
                return Optional.empty();
            }
        }

        @Getter
        @RequiredArgsConstructor
        class StdRawResponse implements RawResponse {

            Head head;
            Mutable<Reader> stdOut;
            Mutable<Reader> stdErr;

            public StdRawResponse(Head head, Reader stdOut, Reader stdErr) {
                this.head = head;
                this.stdOut = Mutable.of(stdOut);
                this.stdErr = Mutable.of(stdErr);
            }

            @Override
            public Optional<Reader> bodyReader() {
                return Optional.empty();
            }

            @Override
            public Optional<Reader> stdOutReader() {
                return stdOut.maybe();
            }

            @Override
            public Optional<Reader> stdErrReader() {
                return stdErr.maybe();
            }
        }
    }

    interface Head {

        Integer status();

        Map<String, String> headers();
    }

    interface StringBody extends Body<String> {
    }

    interface Body<T> {

        @SuppressWarnings("unchecked")
        static <T> Body<T> empty() {
            return EmptyBody.instance();
        }

        T value();

        @SuppressWarnings("rawtypes")
        class EmptyBody implements Body {

            private static final EmptyBody INSTANCE = new EmptyBody();

            private EmptyBody() {
            }

            private static EmptyBody instance() {
                return INSTANCE;
            }

            @Override
            public Object value() {
                throw new NoSuchElementException();
            }
        }
    }
}
