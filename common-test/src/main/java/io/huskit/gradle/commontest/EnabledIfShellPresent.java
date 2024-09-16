package io.huskit.gradle.commontest;

import org.junit.jupiter.api.condition.EnabledIf;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface EnabledIfShellPresent {

    @Retention(RetentionPolicy.RUNTIME)
    @EnabledIf("io.huskit.gradle.commontest.ShellConditions#cmdAvailable")
    @interface Cmd {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @EnabledIf("io.huskit.gradle.commontest.ShellConditions#powershellAvailable")
    @interface PowerShell {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @EnabledIf("io.huskit.gradle.commontest.ShellConditions#bashAvailable")
    @interface Bash {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @EnabledIf("io.huskit.gradle.commontest.ShellConditions#shAvailable")
    @interface Sh {
    }
}
