package io.huskit.containers.http;

import io.huskit.containers.api.container.run.HtCreateSpec;
import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.internal.HtJson;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

class HttpCreateSpec implements HtCreateSpec {

    private static final String requestFormat = "%s %s HTTP/1.1%n"
            + "Host: %s%n"
            + "Connection: keep-alive%n"
            + "Content-Type: application/json%n"
            + "Content-Length: %d%n"
            + "%n";
    Map<String, Object> body;

    public HttpCreateSpec() {
        this.body = new HashMap<>();
    }

    @Override
    public HttpCreateSpec withLabels(Map<String, ?> labels) {
        if (!labels.isEmpty()) {
            var stringLabels = new HashMap<String, String>(labels.size());
            for (var entry : labels.entrySet()) {
                var value = entry.getValue();
                if (value == null) {
                    throw new IllegalArgumentException("Label value cannot be null");
                }
            }
            body.put("Labels", stringLabels);
        }
        return this;
    }

    @Override
    public HttpCreateSpec withEnv(Map<String, ?> env) {
        if (!env.isEmpty()) {
            body.put(
                    "Env",
                    env.entrySet().stream()
                            .map(entry -> entry.getKey() + "=" + entry.getValue())
                            .collect(Collectors.toList())
            );
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public HttpCreateSpec withRemove() {
        var hostConfig = body.get("HostConfig");
        if (hostConfig == null) {
            var c = new HashMap<String, Object>();
            c.put("AutoRemove", true);
            body.put("HostConfig", c);
            return this;
        }
        var c = (Map<String, Object>) hostConfig;
        c.put("AutoRemove", true);
        return this;
    }

    @Override
    public HttpCreateSpec withPortBinding(Number hostPort, Number containerPort) {
        return withPortBindings(Map.of(containerPort, hostPort));
    }

    @Override
    public HttpCreateSpec withPortBindings(Map<? extends Number, ? extends Number> portBindings) {
        var pb = new HashMap<String, Object>();
        portBindings.forEach((containerPort, hostPort) -> {
            pb.put(containerPort.toString() + "/tcp", Map.of("HostPort", hostPort.toString()));
        });
        var hostConfig = new HashMap<String, Object>();
        hostConfig.put("PortBindings", pb);
        body.put("HostConfig", hostConfig);
        return this;
    }

    @Override
    public HttpCreateSpec withCommand(CharSequence command, Object... args) {
        return withCommand(command, Arrays.asList(args));
    }

    @Override
    public HttpCreateSpec withCommand(CharSequence command, Iterable<?> args) {
        var cmd = new ArrayList<String>();
        cmd.add(command.toString());
        for (var arg : args) {
            cmd.add(arg.toString());
        }
        body.put("Cmd", cmd);
        return this;
    }

    public Http.Request toRequest(HtImgName image) {
        this.body.put("Image", image.reference());
        var body = HtJson.toJson(this.body);
        var bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        var contentLength = bodyBytes.length;
        var request = String.format(
                requestFormat,
                "POST",
                "/containers/create",
                "localhost",
                contentLength
        ) + body;
        return new DfHttpRequest(request.getBytes(StandardCharsets.UTF_8));
    }
}
