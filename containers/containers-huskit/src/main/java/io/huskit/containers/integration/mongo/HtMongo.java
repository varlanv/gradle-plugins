package io.huskit.containers.integration.mongo;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import io.huskit.containers.api.HtDocker;
import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.integration.*;

import java.time.Duration;
import java.util.function.Consumer;

public class HtMongo implements HtContainer, HtStartedContainer {

    HtDockerImageName imageName;
    DefContainerSpec containerSpec;
    DefDockerClientSpec dockerClientSpec;
    Mutable<Boolean> reuse;

    public HtMongo(HtDockerImageName imageName) {
        this.imageName = imageName;
        this.containerSpec = new DefContainerSpec();
        this.dockerClientSpec = new DefDockerClientSpec(this);
        this.containerSpec.await().forLogMessageContaining("Waiting for connections");
        this.reuse = Volatile.of(false);
    }

   public static HtMongo fromImage(CharSequence image) {
        return new HtMongo(HtDockerImageName.of(image));
    }

    @Override
    public HtContainer withReuse() {
        reuse.set(true);
        return this;
    }

    @Override
    public HtContainer withDockerClientSpec(Consumer<DockerClientSpec> dockerClientSpecAction) {
        dockerClientSpecAction.accept(dockerClientSpec);
        return this;
    }

    @Override
    public HtMongo withContainerSpec(Consumer<ContainerSpec> containerSpecAction) {
        containerSpecAction.accept(containerSpec);
        return this;
    }

    @Override
    public HtStartedContainer start() {
        dockerClientSpec.docker().or(() -> {
                    var docker = HtDocker.anyClient();
                    if (!reuse.require()) {
                        return docker.withCleanOnClose(true);
                    }
                    return docker;
                })
                .containers()
                .run(imageName.id(), runSpec -> {
                            var envSpec = containerSpec.envSpec();
                            envSpec.envMap().ifPresent(runSpec::withEnv);
                            var waitSpec = containerSpec.waitSpec();
                            waitSpec.textWait().ifPresent(waiter -> runSpec.withLookFor(waiter.text(), waiter.duration()));
                        }
                ).exec();
        return this;
    }

    @SuppressWarnings("all")
    public static void main(String[] args) {
        var fullTime = System.currentTimeMillis();
        HtMongo.fromImage("mongo:4.4.8")
//                .withDockerClientSpec(dockerClientSpec -> dockerClientSpec.withDocker(
//                        HtDocker.cli().configure((HtCliDocker.HtCliConsumer) htCliDockerSpec ->
//                                htCliDockerSpec.withCleanOnClose(false))))
                .withContainerSpec(containerSpec ->
                        containerSpec
                                .env().pair("KEaA", "vaka")
                )
                .start();
        int count = 0;
        System.out.println(count);
//        var id = HtDocker.cli()
//                .configure(spec -> spec.withCleanOnClose(true).withShell(ShellType.BASH))
//                .containers()
//                .run("mongo:4.4.8")
//                .exec().id();
        System.out.println("Time: " + Duration.ofMillis(System.currentTimeMillis() - fullTime));
    }


}
