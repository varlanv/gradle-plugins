package io.huskit.containers.api.run;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RunCommand {

    String command;
    List<String> args;
}
