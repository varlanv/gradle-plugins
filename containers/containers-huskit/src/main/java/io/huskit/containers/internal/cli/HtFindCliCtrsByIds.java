package io.huskit.containers.internal.cli;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.internal.HtContainerFromMap;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtFindCliCtrsByIds {

    HtCli cli;
    Set<String> ids;

    @SneakyThrows
    public Stream<HtContainer> stream() {
        try {
            var containers = cli.sendCommand(
                    new CliCommand(CommandType.CONTAINERS_INSPECT, buildListContainersCommand(ids)).withLinePredicate(Predicate.not(String::isBlank)),
                    result -> {
                        return result.lines().stream()
                                .map(HtJson::toMap)
                                .map(map -> (HtContainer) new HtContainerFromMap(map))
                                .collect(Collectors.toList());
                    });
            return containers.stream();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to find containers by ids", e);
        }
    }

    private List<String> buildListContainersCommand(Set<String> ids) {
        var staticArgsSize = 4;
        var command = new ArrayList<String>(staticArgsSize + ids.size());
        command.add("docker");
        command.add("inspect");
        command.add("--format");
        command.add("\"{{json .}}\"");
        command.addAll(ids);
        return command;
    }
}
