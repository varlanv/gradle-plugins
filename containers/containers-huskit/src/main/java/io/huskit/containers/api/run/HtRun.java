package io.huskit.containers.api.run;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.HtDockerImageName;

import java.util.function.Function;

public interface HtRun {

    HtRun withImage(String image);

    HtRun withImage(HtDockerImageName dockerImageName);

    HtRun withOptions(Function<HtRunOptions, HtRunOptions> options);

    HtRun withCommand(Function<HtRunCommandSpec, HtRunCommand> command);

    HtContainer exec();
}
