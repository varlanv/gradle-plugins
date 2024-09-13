package io.huskit.containers.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

@Getter
@RequiredArgsConstructor
public class HtCliDckrSpec implements HtCliDockerSpec {

    @With
    CliRecorder recorder;

    @Override
    public HtCliDockerSpec withCliRecorder(CliRecorder recorder) {
        return this.withRecorder(recorder);
    }
}
