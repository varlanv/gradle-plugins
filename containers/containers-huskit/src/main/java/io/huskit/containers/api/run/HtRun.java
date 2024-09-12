package io.huskit.containers.api.run;

import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.api.HtContainer;

import java.util.function.Function;

public interface HtRun {

    HtRun withImage(String image);

    HtRun withImage(HtDockerImageName dockerImageName);

    HtRun withOptions(Function<HtRunOptionsBuilder, HtRunOptions> options);

    HtRun withCommand(Function<HtRunCommandBuilder, HtRunCommand> command);

    HtContainer exec();
}
