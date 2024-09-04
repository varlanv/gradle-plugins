package io.huskit.gradle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.testkit.runner.BuildResult;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class GradleRunResult {

    List<BuildResult> buildResults;

    public MarkedMessages findMarkedMessages(String messagePattern) {
        return new MarkedMessages(
                findMarkedMessages(buildResults.get(0), messagePattern),
                findMarkedMessages(buildResults.get(1), messagePattern)
        );
    }

    private List<MarkedMessage> findMarkedMessages(BuildResult buildResult, String messagePattern) {
        return Arrays.stream(buildResult.getOutput().split(System.lineSeparator()))
                .map(String::trim)
                .filter(line -> line.startsWith("~_~~"))
                .filter(line -> line.contains(messagePattern))
                .map(line -> line.substring(4))
                .map(MarkedMessage::new)
                .collect(Collectors.toList());
    }
}
