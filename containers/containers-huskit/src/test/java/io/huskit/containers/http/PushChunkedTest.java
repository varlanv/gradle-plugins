package io.huskit.containers.http;

import io.huskit.common.Sneaky;
import io.huskit.containers.internal.HtJson;
import io.huskit.gradle.commontest.UnitTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PushChunkedTest implements UnitTest {

    byte[] body = Sneaky.quiet(() -> IOUtils.resourceToByteArray("/docker_http_responses/containers/chunked_json_body.txt"));

    @Test
    void push__when_body_is_present__should_return_chunked() {
        var subject = new PushChunked<>(Function.identity());

        var maybeActual = subject.apply(ByteBuffer.wrap(body));

        assertThat(maybeActual)
            .isPresent()
            .hasValueSatisfying(actual -> {
                var jsonArray = HtJson.toMapList(actual);
                assertThat(jsonArray).isNotEmpty();
            });
    }
}
