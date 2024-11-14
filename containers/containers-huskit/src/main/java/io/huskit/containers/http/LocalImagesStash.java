package io.huskit.containers.http;

import io.huskit.containers.api.image.HtImages;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
final class LocalImagesStash {

    private static final Map<String, Lock> imageToLock = new ConcurrentHashMap<>();
    private static final Set<String> pulledImages = new ConcurrentSkipListSet<>();
    private static final AtomicBoolean isInitialized = new AtomicBoolean();
    private static final Lock initLock = new ReentrantLock();
    HtImages htImages;

    void pullIfAbsent(String image) {
        if (!isInitialized.get()) {
            initLock.lock();
            try {
                if (!isInitialized.get()) {
                    htImages.list().stream()
                        .forEach(img -> img.inspect()
                            .tags()
                            .forEach(imageTag -> pulledImages.add(imageTag.repository() + ":" + imageTag.tag()))
                        );
                    isInitialized.set(true);
                }
            } finally {
                initLock.unlock();
            }
        }
        if (pulledImages.contains(image)) {
            return;
        }
        var lock = imageToLock.computeIfAbsent(image, key -> new ReentrantLock());
        lock.lock();
        if (!pulledImages.contains(image)) {
            try {
                htImages.pull(image).exec();
                pulledImages.add(image);
            } finally {
                lock.unlock();
            }
        } else {
            lock.unlock();
        }
    }
}
