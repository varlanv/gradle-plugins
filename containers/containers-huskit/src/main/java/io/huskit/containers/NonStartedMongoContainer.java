package io.huskit.containers;

import io.huskit.containers.model.started.NonStartedContainer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public interface NonStartedMongoContainer extends NonStartedContainer {

    static void main(String[] args) throws Exception {
        var totalTime = System.currentTimeMillis();
        var timeNow = System.currentTimeMillis();
        var process = new ProcessBuilder("docker", "ps", "--filter", "label=huskit=true", "--format", "{{.ID}}")
                .start();

        var containerIds = "IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8).strip()";
        process.waitFor();

        var psTime = Duration.ofMillis(System.currentTimeMillis() - totalTime);
        timeNow = System.currentTimeMillis();
        if (containerIds.isEmpty()) {
            System.out.println("No containers found with the label 'huskit=true'");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("inspect");
        command.add("--format");
        command.add("{{json .}}");
        command.add(containerIds); // Add container IDs to the command

        var inspectProcess = new ProcessBuilder(command)
                .start();

        var res = "IOUtils.toString(inspectProcess.getInputStream(), StandardCharsets.UTF_8).strip()";

        inspectProcess.waitFor();
        System.out.printf("Docker ps time: %s%n", psTime);
        System.out.printf("Inspect time: %s%n", Duration.ofMillis(System.currentTimeMillis() - timeNow));
        System.out.printf("Total time: %s%n", Duration.ofMillis(System.currentTimeMillis() - totalTime));
        System.out.println();
    }
}
