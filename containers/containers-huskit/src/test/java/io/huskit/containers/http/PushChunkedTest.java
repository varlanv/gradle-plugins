package io.huskit.containers.http;

import io.huskit.common.Sneaky;
import io.huskit.containers.internal.HtJson;
import io.huskit.gradle.commontest.UnitTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PushChunkedTest implements UnitTest {

    String body = Sneaky.quiet(() -> IOUtils.resourceToString("/docker_http_responses/containers/chunked_json_body.txt", StandardCharsets.UTF_8));

    @Test
    void push__when_body_is_present__should_return_chunked_crlf() {
        var subject = new PushChunked<>(new PushRaw());

        var maybeActual = subject.apply(ByteBuffer.wrap(body.replace("\r\n", "\n").replace("\n", "\r\n").getBytes(StandardCharsets.UTF_8)));

        assertThat(maybeActual)
            .isPresent()
            .hasValueSatisfying(actual -> {
                var jsonArray = HtJson.toMapList(actual);
                assertThat(jsonArray).isNotEmpty();
            });
    }

    @Test
    void push__when_body_is_present__should_return_chunked_lf() {
        var subject = new PushChunked<>(new PushRaw());

        var maybeActual = subject.apply(ByteBuffer.wrap(body.replace("\r\n", "\n").getBytes(StandardCharsets.UTF_8)));

        assertThat(maybeActual)
            .isPresent()
            .hasValueSatisfying(actual -> {
                var jsonArray = HtJson.toMapList(actual);
                assertThat(jsonArray).isNotEmpty();
            });
    }
}
