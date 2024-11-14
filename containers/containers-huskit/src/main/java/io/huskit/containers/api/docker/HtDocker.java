package io.huskit.containers.api.docker;

import io.huskit.common.Log;
import io.huskit.containers.api.container.HtContainers;
import io.huskit.containers.api.image.HtImages;
import io.huskit.containers.api.volume.HtVolumes;
import io.huskit.containers.http.HtHttpDckr;
import io.huskit.containers.http.HtHttpDocker;

import java.time.Duration;

public interface HtDocker {

    HtDocker withDefaultTimeout(Duration timeout);

    HtDocker withCleanOnClose(Boolean cleanOnClose);

    HtDocker withLog(Log log);

    HtContainers containers();

    HtImages images();

    HtVolumes volumes();

    static HtDocker anyClient() {
        return http();
    }

    static HtHttpDocker http() {
        return new HtHttpDckr().withCleanOnClose(true);
    }
}
