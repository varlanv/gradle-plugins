package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class DfResponse implements Http.Response {

    Http.Head head;
    Http.Body body;
}
