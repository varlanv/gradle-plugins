package io.huskit.gradle.commontest;

import lombok.AccessLevel;
import lombok.Locked;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.condition.OS;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShellConditions {

    private static final long TIMEOUT = 2000L; // Timeout in milliseconds
    private static final ConcurrentMap<String, Boolean> CACHE = new ConcurrentHashMap<>();

    @Locked
    public static boolean cmdAvailable() {
        return CACHE.computeIfAbsent("cmd", key -> isCommandAvailable(List.of("cmd", "/c", "echo")));
    }

    @Locked
    public static boolean powershellAvailable() {
        return CACHE.computeIfAbsent("powershell", key -> isCommandAvailable(List.of("powershell", "-Command", "echo 'test'")));
    }

    @Locked
    public static boolean bashAvailable() {
        return CACHE.computeIfAbsent("bash", key -> {
            if (OS.current() == OS.WINDOWS) {
                return isCommandAvailable(List.of("C:\\Program Files\\Git\\bin\\bash.exe", "-c", "echo test"));
            } else {
                return isCommandAvailable(List.of("bash", "-c", "echo test"));
            }
        });
    }

    @Locked
    public static boolean shAvailable() {
        return CACHE.computeIfAbsent("sh", key -> isCommandAvailable(List.of("sh", "-c", "echo test")));
    }

    @SneakyThrows
    private static boolean isCommandAvailable(List<String> command) {
        try {
            var process = new ProcessBuilder(command).start();
            return checkSuccess(process);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean checkSuccess(Process process) {
        try {
            boolean finishedInTime = process.waitFor(TIMEOUT, TimeUnit.MILLISECONDS);
            return finishedInTime && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        } finally {
            process.destroyForcibly();
        }
    }
}
