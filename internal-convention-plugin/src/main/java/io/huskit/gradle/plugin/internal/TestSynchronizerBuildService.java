package io.huskit.gradle.plugin.internal;

import lombok.SneakyThrows;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class TestSynchronizerBuildService implements BuildService<BuildServiceParameters.None>, AutoCloseable, Serializable {

    static final String name = "__internal_huskit_plugin_bs__";
//    private final long seed = new Random().nextLong();
//    private final String TEMP_FOLDER_PATH_STR = System.getProperty("java.io.tmpdir");
//    private final String SYNC_FILE_NAME = "sync.txt";
//    private final Path TEMP_FOLDER_PATH = Path.of(TEMP_FOLDER_PATH_STR);
//    private final Path SYNC_FOLDER_PATH = TEMP_FOLDER_PATH.resolve("huskitjunitsync" + seed);
//    private final Path SYNC_FILE_PATH = SYNC_FOLDER_PATH.resolve(SYNC_FILE_NAME);
//    private final Lock syncFileCreationLock = new ReentrantLock();
//    private final String syncFilePath = SYNC_FILE_PATH.toString();

//    @SneakyThrows
//    public String syncFilePath() {
//        if (!Files.exists(SYNC_FILE_PATH)) {
//            syncFileCreationLock.lock();
//            try {
//                if (!Files.exists(SYNC_FILE_PATH)) {
//                    if (!Files.exists(SYNC_FOLDER_PATH)) {
//                        Files.createDirectories(SYNC_FOLDER_PATH);
//                        Files.createFile(SYNC_FILE_PATH);
//                    } else {
//                        Files.createFile(SYNC_FILE_PATH);
//                    }
//                }
//            } finally {
//                syncFileCreationLock.unlock();
//            }
//        }
//        return syncFilePath;
//    }

    @Override
    public void close() throws Exception {
//        if (Files.exists(SYNC_FOLDER_PATH)) {
//            Files.delete(SYNC_FILE_PATH);
//            Files.delete(SYNC_FOLDER_PATH);
//        }
    }
}
