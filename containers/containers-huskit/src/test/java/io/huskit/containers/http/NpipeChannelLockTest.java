package io.huskit.containers.http;

import io.huskit.common.Log;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NpipeChannelLockTest implements UnitTest {

    @Test
    void when_release_lock_without_take_lock_then_throw_exception() {
        var log = Log.fake();
        var subject = new NpipeChannelLock(log);

        assertThatThrownBy(subject::releaseLock)
            .hasMessageContaining("not acquired");
        assertThat(log.debugMessages()).isEmpty();
    }

    @Test
    void when_acquire_lock__then_take_lock() {
        var log = Log.fake();
        var subject = new NpipeChannelLock(log);

        subject.acquire(() -> "request");

        assertThat(log.debugMessages()).containsOnly("Took lock for request -> request");
        assertThat(subject.isAcquired()).isTrue();
    }

    @Test
    void when_locked_and_new_thread_tries_to_acquire__then_wait_for_lock() throws InterruptedException {
        var log = Log.fake();
        var subject = new NpipeChannelLock(log);
        subject.acquire(() -> "request");

        var latch = new CountDownLatch(1);
        var thread = new Thread(() -> {
            latch.countDown();
            subject.acquire(() -> "new request");
        });
        try {
            thread.start();
            Thread.sleep(20);

            assertThat(log.debugMessages()).containsOnly("Took lock for request -> request");
            assertThat(subject.isAcquired()).isTrue();
        } finally {
            thread.interrupt();
        }
    }

    @Test
    void when_locked_and_new_thread_tries_to_acquire_and_release__then_get_lock_by_new_thread() throws InterruptedException {
        var log = Log.fake();
        var subject = new NpipeChannelLock(log);
        subject.acquire(() -> "request");

        var startLatch = new CountDownLatch(1);
        var completionLatch = new CountDownLatch(1);
        var thread = new Thread(() -> {
            startLatch.countDown();
            subject.acquire(() -> "new request");
            completionLatch.countDown();
        });
        try {
            thread.start();
            startLatch.await();
            subject.releaseLock();
            completionLatch.await();

            assertThat(subject.isAcquired()).isTrue();
            var debugMessages = log.debugMessages();
            assertThat(debugMessages).hasSize(3);
            assertThat(debugMessages.get(0)).isEqualTo("Took lock for request -> request");
            assertThat(debugMessages.get(1)).contains("Releasing lock, `lockTime` -> ");
            assertThat(debugMessages.get(2)).isEqualTo("Took lock for request -> new request");
        } finally {
            thread.interrupt();
        }
    }
}
