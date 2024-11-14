package io.huskit.containers.http;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class DockerOnShutdown implements Runnable {

    private static final Queue<Runnable> actions = new ConcurrentLinkedDeque<>();

    static {
        Runtime.getRuntime().addShutdownHook(
            new Thread(new DockerOnShutdown())
        );
    }

    private DockerOnShutdown() {
    }

    @Override
    public void run() {
        for (var action : actions) {
            action.run();
        }
    }

    public static void register(Runnable action) {
        actions.add(action);
    }
}
