package io.huskit.gradle.commontest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;

@Tag("unit-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseStatelessUnitTest extends BaseTest {
}
