package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.run.HtRun;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
final class HttpRun implements HtRun {

    HttpCreate httpCreate;
    Function<String, HttpStart> httpStartFromContainerId;

    @Override
    public HtContainer exec() {
        var container = httpCreate.exec();
        return httpStartFromContainerId.apply(container.id()).exec();
    }
}
