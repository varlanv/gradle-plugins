package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
final class DfHead implements Http.Head {

    Integer status;
    Map<String, String> headers;
}
