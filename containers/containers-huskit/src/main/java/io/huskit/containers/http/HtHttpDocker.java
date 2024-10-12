package io.huskit.containers.http;

import io.huskit.containers.api.docker.HtDocker;

public interface HtHttpDocker extends HtDocker {

    void close();
}
