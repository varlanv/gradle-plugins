package io.huskit.containers.http;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Reader;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

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

        SimplePipe stdOut();

        SimplePipe stdErr();

        @RequiredArgsConstructor
        final class BodyRawResponse implements RawResponse {

            @Getter
            Head head;
            @NonNull
            Supplier<Reader> bodyReader;

            @Override
            public Reader bodyReader() {
                return bodyReader.get();
            }

            @Override
            public SimplePipe stdOut() {
                throw new NoSuchElementException();
            }

            @Override
            public SimplePipe stdErr() {
                throw new NoSuchElementException();
            }
        }

        @Getter
        @RequiredArgsConstructor
        final class StdRawResponse implements RawResponse {

            Head head;
            SimplePipe stdOut;
            SimplePipe stdErr;

            @Override
            public Reader bodyReader() {
                throw new RuntimeException("Body not available");
            }
        }

        @Getter
        @RequiredArgsConstructor
        final class OnlyHeadRawResponse implements RawResponse {

            Head head;

            @Override
            public Reader bodyReader() {
                throw new NoSuchElementException();
            }

            @Override
            public SimplePipe stdOut() {
                throw new NoSuchElementException();
            }

            @Override
            public SimplePipe stdErr() {
                throw new NoSuchElementException();
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
        final class EmptyBody implements Body {

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
