package io.huskit.containers.cli;

import io.huskit.containers.HtContainerFromMap;
import io.huskit.containers.api.HtContainer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtFindCliContainersByIds {

    HtCli cli;
    Set<String> ids;

    @SneakyThrows
    public Stream<HtContainer> stream() {
        var containers = cli.sendCommand(
                new CliCommand(buildListContainersCommand(ids)).withLinePredicate(Predicate.not(String::isBlank)),
                result -> {
                    return result.lines().stream()
                            .map(JSONObject::new)
                            .map(JSONObject::toMap)
                            .map(map -> (HtContainer) new HtContainerFromMap(map))
                            .collect(Collectors.toList());
                });
        return containers.stream();
    }

    private List<String> buildListContainersCommand(Set<String> ids) {
        var command = new ArrayList<String>(4 + ids.size());
        command.add("docker");
        command.add("inspect");
        command.add("--format");
        command.add("\"{{json .}}\"");
        command.addAll(ids);
        return command;
    }
}
