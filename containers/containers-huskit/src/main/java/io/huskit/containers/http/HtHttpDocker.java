package io.huskit.containers.http;

import io.huskit.common.Log;
import io.huskit.containers.api.docker.HtDocker;

import java.time.Duration;

public interface HtHttpDocker extends HtDocker {

    @Override
    HtHttpDocker withCleanOnClose(Boolean cleanOnClose);

    @Override
    HtHttpDocker withLog(Log log);

    @Override
    HtHttpDocker withDefaultTimeout(Duration timeout);

    void close();
}
