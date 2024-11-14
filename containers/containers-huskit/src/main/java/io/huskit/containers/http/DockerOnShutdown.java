package io.huskit.containers.http;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ForkJoinPool;

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
        if (actions.size() == 1) {
            actions.poll().run();
        } else {
            for (var action : actions) {
                ForkJoinPool.commonPool().execute(action);
            }
        }
    }

    public static void register(Runnable action) {
        actions.add(action);
    }
}
