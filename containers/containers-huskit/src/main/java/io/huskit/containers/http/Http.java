package io.huskit.containers.http;

import io.huskit.common.Mutable;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;

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

        Reader bodyReader();

        Reader stdOutReader();

        Reader stdErrReader();

        @RequiredArgsConstructor
        class BodyRawResponse implements RawResponse {

            @Getter
            Head head;
            @NonNull
            Reader bodyReader;

            @Override
            public Reader bodyReader() {
                return bodyReader;
            }

            @Override
            public Reader stdOutReader() {
                throw new RuntimeException("StdOut not available");
            }

            @Override
            public Reader stdErrReader() {
                throw new RuntimeException("StdErr not available");
            }
        }

        @Getter
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
            public Reader bodyReader() {
                throw new RuntimeException("Body not available");
            }

            @Override
            public Reader stdOutReader() {
                return stdOut.require();
            }

            @Override
            public Reader stdErrReader() {
                return stdErr.require();
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
