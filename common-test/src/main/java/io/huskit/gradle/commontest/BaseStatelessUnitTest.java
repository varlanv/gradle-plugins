package io.huskit.gradle.commontest;

import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseStatelessUnitTest extends BaseUnitTest {
}
