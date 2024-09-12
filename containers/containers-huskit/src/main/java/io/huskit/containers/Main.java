package io.huskit.containers;

import io.huskit.containers.api.HtDocker;
import io.huskit.containers.api.logs.LookFor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.stream.Collectors;

public class Main {

    @SneakyThrows
    public static void main(String[] args) throws Exception {
        var time = System.currentTimeMillis();
        var docker = HtDocker.cli();
        for (int i = 0; i < 2; i++) {
            var container = docker.run("mongo:4.4.8").exec();
            System.out.println("name - " + container.name() + " id - " + container.id());
            var logs = docker.logs(container.id())
                    .lookFor(LookFor.word("Waiting for connections"))
                    .stream()
                    .collect(Collectors.toList());
            System.out.println(logs.size());
            System.out.printf("Finished in %s%n", Duration.ofMillis(System.currentTimeMillis() - time));
            System.out.println("-".repeat(50));
            System.out.println("-".repeat(50));
            System.out.println("-".repeat(50));
            System.out.println("-".repeat(50));
        }
    }
}
