package io.huskit.containers.testcontainers.mongo;

import io.huskit.gradle.commontest.GradleIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MongoContainerIntegrationTest implements GradleIntegrationTest {

    String databaseName = "someDatabaseName";

    @Test
    @DisplayName("When same reusable mongo container is requested twice - only one container is started")
    void test_0() {
//        setupProject()
//        test(fixture -> {
//            var requestedContainer = buildMongoContainerRequest(fixture, "project1", fixture.id, true);
//            var containersRequest = prepareContainerRequest(
//                    fixture.log,
//                    List.of(requestedContainer)
//            );
//
//            var list1 = fixture.application.containers(containersRequest).start().list();
//            assertThat(list1).hasSize(1);
//            assertThat(list1.get(0).id().json()).isEqualTo(fixture.id);
//            assertThat(list1.get(0).port().number()).isNotEqualTo(0);
//
//            var list2 = fixture.application.containers(containersRequest).start().list();
//            assertThat(list2).hasSize(1);
//            assertThat(list2.get(0).id().json()).isEqualTo(fixture.id);
//            assertThat(list2.get(0).port().number()).isEqualTo(list1.get(0).port().number());
//
//            var huskitContainersWithId = DockerUtil.findHuskitContainersWithId(fixture.id);
//            assertThat(huskitContainersWithId).hasSize(1);
//        });
    }

    @Test
    @DisplayName("When same non reusable mongo container is requested twice with different sources - create two containers")
    void test_1() {
//        test(fixture -> {
//            var request1 = prepareContainerRequest(
//                    fixture.log,
//                    buildMongoContainerRequest(fixture, "project1", fixture.id, false)
//            );
//            var request2 = prepareContainerRequest(
//                    fixture.log,
//                    buildMongoContainerRequest(fixture, "project2", fixture.id, false)
//            );
//
//            var list1 = fixture.application.containers(request1).start().list();
//            var huskitContainersWithId = DockerUtil.findHuskitContainersWithId(fixture.id);
//            assertThat(huskitContainersWithId).hasSize(1);
//            assertThat(list1).hasSize(1);
//            assertThat(list1.get(0).id().json()).isEqualTo(fixture.id);
//            assertThat(list1.get(0).port().number()).isNotEqualTo(0);
//
//            var list2 = fixture.application.containers(request2).start().list();
//            huskitContainersWithId = DockerUtil.findHuskitContainersWithId(fixture.id);
//            assertThat(huskitContainersWithId).hasSize(2);
//            assertThat(list2).hasSize(1);
//            assertThat(list2.get(0).id().json()).isEqualTo(fixture.id);
//            assertThat(list2.get(0).port().number()).isNotEqualTo(list1.get(0).port().number());
//            var huskitContainersWithId2 = DockerUtil.findHuskitContainersWithId(fixture.id);
//            assertThat(huskitContainersWithId2).hasSize(2);
//        });
    }

//    private DefaultMongoRequestedContainer buildMongoContainerRequest(Fixture fixture,
//                                                                      String source,
//                                                                      String id,
//                                                                      boolean isAllowedReuse) {
//        return new DefaultMongoRequestedContainer(
//                new DefaultRequestedContainer(
//                        source,
//                        MongoContainer.DEFAULT_IMAGE,
//                        id,
//                        fixture.port,
//                        ContainerType.MONGO,
//                        new DefaultMongoContainerReuse(isAllowedReuse, isAllowedReuse, isAllowedReuse)
//                ),
//                databaseName
//        );
//    }
}
