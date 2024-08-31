package io.huskit.gradle;

import org.junit.platform.commons.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkedMessages {

    public final List<MarkedMessage> forFirstBuild;
    public final List<MarkedMessage> forSecondBuild;

    public MarkedMessages(List<MarkedMessage> forFirstBuild, List<MarkedMessage> forSecondBuild) {
        this.forFirstBuild = List.copyOf(forFirstBuild);
        this.forSecondBuild = List.copyOf(forSecondBuild);
    }

    public List<String> values() {
        return Stream.of(forFirstBuild, forSecondBuild)
                .flatMap(List::stream)
                .map(m -> m.value)
                .map(value -> value.split("=")[1])
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }
}
