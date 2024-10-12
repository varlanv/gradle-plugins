package io.huskit.containers.http;

import java.util.Map;

public interface Head {

    int status();

    Map<String, String> headers();
}
