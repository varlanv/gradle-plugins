package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.containers.api.container.run.HtRmSpec;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@RequiredArgsConstructor
final class HttpRmSpec implements HtRmSpec {

    private static final String requestFormat = "%s %s HTTP/1.1%n"
        + "Host: %s%n"
        + "Connection: keep-alive%n"
        + "Content-Type: application/json%n"
        + "%n";
    Mutable<Boolean> force;
    Mutable<Boolean> volumes;

    public HttpRmSpec() {
        this.force = Mutable.of(false);
        this.volumes = Mutable.of(false);
    }

    @Override
    public HtRmSpec withForce(Boolean force) {
        this.force.set(force);
        return this;
    }

    @Override
    public HtRmSpec withVolumes(Boolean volumes) {
        this.volumes.set(volumes);
        return this;
    }

    public Http.Request toRequest(CharSequence containerId) {
        var url = getUrl(containerId.toString());
        return new DfHttpRequest(
            String.format(requestFormat, "DELETE", url, "localhost").getBytes(StandardCharsets.UTF_8)
        );
    }

    private String getUrl(String containerId) {
        var isForce = force.require();
        var isVolumes = volumes.require();
        var params = new ArrayList<String>(3);
        if (isForce) {
            params.add("force=true");
        }
        if (isVolumes) {
            params.add("v=true");
        }
        if (params.isEmpty()) {
            return "/containers/" + containerId;
        } else {
            return "/containers/" + containerId + "?" + String.join("&", params);
        }
    }
}
