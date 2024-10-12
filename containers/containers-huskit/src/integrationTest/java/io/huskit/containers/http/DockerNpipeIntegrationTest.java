package io.huskit.containers.http;

import io.huskit.gradle.commontest.DockerIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

public class DockerNpipeIntegrationTest implements DockerIntegrationTest {

    @Test
    void list_containers() throws Exception {
        var subject = new DockerNpipe();
        try {
            var latch = new CountDownLatch(1);
//            subject.test(kek -> {
//                System.out.println(kek.head().status());
//                System.out.println(kek.head().headers());
//                System.out.println(kek.body().list());
//                latch.countDown();
//            });
//            latch.await();
        } finally {
            subject.close();
        }
    }
}
