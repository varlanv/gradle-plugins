package io.huskit.containers.internal;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class Io {

    @SneakyThrows
    public void readLines(InputStream stream, Consumer<String> lineReader) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineReader.accept(line);
            }
        }
    }
}
