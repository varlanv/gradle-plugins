package io.huskit.gradle.commontest;

import org.apache.commons.lang3.time.StopWatch;

import java.util.concurrent.atomic.AtomicInteger;

public class TestStopWatch {

    AtomicInteger counter = new AtomicInteger();
    StopWatch stopWatch = StopWatch.createStarted();

    public void logTime() {
//        System.err.printf("Elapsed time for [%d] mark = %s\n", counter.incrementAndGet(), stopWatch);
//        stopWatch.reset();
//        stopWatch.start();
    }
}
