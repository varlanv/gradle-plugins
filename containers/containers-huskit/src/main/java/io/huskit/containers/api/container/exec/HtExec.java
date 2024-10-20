package io.huskit.containers.api.container.exec;

import java.util.concurrent.CompletableFuture;

public interface HtExec {

    void exec();

    CompletableFuture<Void> execAsync();
}
