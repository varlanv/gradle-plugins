package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AddCommonDependencies {

    AddLombokDependencies addLombokDependencies;
    AddSpockDependencies addSpockDependencies;
    AddTestDependencies addTestDependencies;

    public void add() {
        addLombokDependencies.add();
        addSpockDependencies.add();
        addTestDependencies.add();
    }
}
