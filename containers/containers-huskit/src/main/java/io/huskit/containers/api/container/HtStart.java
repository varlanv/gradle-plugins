package io.huskit.containers.api.container;

import java.util.concurrent.CompletableFuture;

public interface HtStart {

    HtContainer exec();

    CompletableFuture<HtContainer> execAsync();
}
