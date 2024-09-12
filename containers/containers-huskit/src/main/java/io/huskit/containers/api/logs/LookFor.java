package io.huskit.containers.api.logs;

public interface LookFor {

    static LookFor nothing() {
        return LookForNothing.INSTANCE;
    }

    static LookFor word(String word) {
        return () -> word;
    }

    String value();
}
