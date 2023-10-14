package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddCommonDependencies {

    private final AddLombokDependencies addLombokDependencies;
    private final AddSpockDependencies addSpockDependencies;
    private final AddCommonTestDependency addCommonTestDependency;

    public void add() {
        addLombokDependencies.add();
        addSpockDependencies.add();
        addCommonTestDependency.add();
    }
}
