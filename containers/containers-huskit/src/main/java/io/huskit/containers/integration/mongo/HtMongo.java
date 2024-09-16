package io.huskit.containers.integration.mongo;

import io.huskit.containers.api.HtDocker;
import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.api.ShellType;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HtMongo implements HtContainer {

    HtDockerImageName imageName;
    DefContainerSpec containerSpec = new DefContainerSpec();

    static HtMongo fromImage(CharSequence image) {
        return new HtMongo(HtDockerImageName.of(image));
    }

    @Override
    public HtMongo withContainerSpec(Consumer<ContainerSpec> containerSpecAction) {
        containerSpecAction.accept(containerSpec);
        return this;
    }

    public void start() {
        HtDocker.cli()
                .configure(containerSpec ->
                        containerSpec.withCleanOnClose(true))
                .containers()
                .run(
                        imageName.id(),
                        runSpec -> {
                            var envSpec = containerSpec.envSpec();
                            envSpec.envMap().ifPresent(runSpec::withEnv);
                            var waitSpec = containerSpec.waitSpec();
                            waitSpec.logMessage().ifPresent(waiter -> runSpec.withLookFor(waiter.text(), waiter.duration()));
                        }).exec();
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        var fullTime = System.currentTimeMillis();
//        fromImage("mongo:4.4.8")
//                .withContainerSpec(containerSpec ->
//                        containerSpec
//                                .env().pair("KEaA", "vaka")
//                                .await().forLogMessageContaining("Waiting for connections", Duration.ZERO))
//                .start();
//        int count = 0;
//        System.out.println(count);
        var id = HtDocker.cli()
                .configure(spec -> spec.withCleanOnClose(true).withShell(ShellType.GITBASH))
                .containers()
                .run("mongo:4.4.8")
                .exec().id();
        System.out.println("Time: " + Duration.ofMillis(System.currentTimeMillis() - fullTime));

    }
}
