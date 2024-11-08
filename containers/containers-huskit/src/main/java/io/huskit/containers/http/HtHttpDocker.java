package io.huskit.containers.http;

import io.huskit.common.Log;
import io.huskit.containers.api.docker.HtDocker;

public interface HtHttpDocker extends HtDocker {

    @Override
    HtHttpDocker withCleanOnClose(Boolean cleanOnClose);

    @Override
    HtHttpDocker withLog(Log log);

    void close();
}
