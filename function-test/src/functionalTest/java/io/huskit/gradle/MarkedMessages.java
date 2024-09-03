package io.huskit.gradle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class MarkedMessages {

    List<MarkedMessage> forFirstBuild;
    List<MarkedMessage> forSecondBuild;

    public List<String> values() {
        return Stream.of(forFirstBuild, forSecondBuild)
                .flatMap(List::stream)
                .map(MarkedMessage::value)
                .map(value -> value.split("=")[1])
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }
}
