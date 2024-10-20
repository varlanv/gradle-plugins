package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtJsonContainer;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
final class HttpInspect {

    HtHttpDockerSpec dockerSpec;

    public HtContainer inspect(CharSequence id) {
        return inspectAsync(id).join();
    }

    public Stream<HtContainer> inspect(Iterable<? extends CharSequence> containerIds) {
        return StreamSupport.stream(containerIds.spliterator(), false)
                .map(this::inspect);
    }

    public CompletableFuture<HtContainer> inspectAsync(CharSequence id) {
        return dockerSpec.socket().sendAsync(
                new Request(
                        dockerSpec.requests().get(new HttpInspectSpec(id))
                ).withExpectedStatus(200)
        ).thenApply(response ->
                new HtJsonContainer(
                        HtJson.toMap(
                                response.bodyReader().orElseThrow()
                        )
                ));
    }
}
