package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.run.HtRun;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@RequiredArgsConstructor
final class HttpRun implements HtRun {

    HttpCreate httpCreate;
    HttpRunSpec httpRunSpec;
    Function<String, HttpLogs> httpLogsFromContainerId;
    Function<String, HttpStart> httpStartFromContainerId;

    @Override
    public HtContainer exec() {
        return execAsync().join();
    }

    @Override
    public CompletableFuture<HtContainer> execAsync() {
        return httpCreate.execAsync()
                .thenCompose(container -> {
                    var completable = httpStartFromContainerId.apply(container.id()).execAsync();
                    return httpRunSpec.lookFor()
                            .map(lookFor -> {
                                var httpLogs = httpLogsFromContainerId.apply(container.id());
                                return completable.thenCompose(c -> httpLogs.follow()
                                        .lookForAsync(lookFor)
                                        .thenApply(ignored -> container)
                                );
                            })
                            .orElse(completable);
                });
    }
}
