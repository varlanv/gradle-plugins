package io.huskit.containers.testcontainers.mongo;

import io.huskit.ContainersIntegrationTest;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.DefaultRequestedContainer;
import io.huskit.containers.model.id.MongoContainerId;
import io.huskit.containers.model.request.ContainersRequest;
import io.huskit.containers.model.request.DefaultMongoRequestedContainer;
import io.huskit.containers.model.reuse.DefaultMongoContainerReuse;
import io.huskit.gradle.commontest.DockerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoContainerIntegrationTest implements ContainersIntegrationTest {

    String databaseName = "someDatabaseName";
    String source1 = "project1";
    String source2 = "project2";
    String source3 = "project3";

    @Test
    @DisplayName("When same reusable mongo container is requested twice with same source - only one container is started")
    void test_0() {
        runFixture(fixture -> {
            var requestedContainer = buildMongoContainerRequest(fixture, source1, true);
            Supplier<ContainersRequest> containersRequest = () -> prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer)
            );
            var id = requestedContainer.id().json();

            var list1 = fixture.application.containers(containersRequest.get()).start().list();
            assertThat(list1).hasSize(1);
            assertThat(list1.get(0).id().json()).isEqualTo(id);
            assertThat(list1.get(0).port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(containersRequest.get()).start().list();
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(id);
            assertThat(list2.get(0).port().number()).isEqualTo(list1.get(0).port().number());

            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
        });
    }

    @Test
    @DisplayName("When same reusable mongo container is requested twice with different sources - only one container is started")
    void test_1() {
        runFixture(fixture -> {
            var requestedContainer1 = buildMongoContainerRequest(fixture, source1, true);
            var containersRequest1 = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer1)
            );
            var id1 = requestedContainer1.id().json();
            var requestedContainer2 = buildMongoContainerRequest(fixture, source2, true);
            var containersRequest2 = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer2)
            );
            var id2 = requestedContainer2.id().json();

            var list1 = fixture.application.containers(containersRequest1).start().list();
            assertThat(list1).hasSize(1);
            assertThat(list1.get(0).id().json()).isEqualTo(id1);
            assertThat(list1.get(0).port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(containersRequest2).start().list();
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(id2);
            assertThat(list2.get(0).port().number()).isEqualTo(list1.get(0).port().number());

            assertThat(DockerUtil.findHuskitContainersWithId(id1)).hasSize(1);
            assertThat(DockerUtil.findHuskitContainersWithId(id2)).hasSize(1);
        });
    }

    @Test
    @DisplayName("When same non reusable mongo container is requested twice with different sources - create two containers")
    void test_2() {
        runFixture(fixture -> {
            var requestedContainer1 = buildMongoContainerRequest(fixture, source1, false);
            var request1 = prepareContainerRequest(
                    fixture.log,
                    requestedContainer1
            );
            var id1 = requestedContainer1.id().json();

            var requestedContainer2 = buildMongoContainerRequest(fixture, source2, false);
            var request2 = prepareContainerRequest(
                    fixture.log,
                    requestedContainer2
            );
            var id2 = requestedContainer2.id().json();

            var list1 = fixture.application.containers(request1).start().list();
            assertThat(DockerUtil.findHuskitContainersWithId(id1)).hasSize(1);
            assertThat(list1).hasSize(1);
            assertThat(list1.get(0).id().json()).isEqualTo(id1);
            assertThat(list1.get(0).port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(request2).start().list();
            assertThat(DockerUtil.findHuskitContainersWithId(id1)).hasSize(1);
            assertThat(DockerUtil.findHuskitContainersWithId(id2)).hasSize(1);
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(id2);
            assertThat(list2.get(0).port().number()).isNotEqualTo(list1.get(0).port().number());
        });
    }

    @Test
    @DisplayName("When same non reusable mongo container is requested twice with same sources - create one container")
    void test_3() {
        runFixture(fixture -> {
            var requestedContainer = buildMongoContainerRequest(fixture, source1, false);
            var request = prepareContainerRequest(
                    fixture.log,
                    requestedContainer
            );
            var id = requestedContainer.id().json();

            var list1 = fixture.application.containers(request).start().list();
            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
            assertThat(list1).hasSize(1);
            assertThat(list1.get(0).id().json()).isEqualTo(id);
            assertThat(list1.get(0).port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(request).start().list();
            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(id);
            assertThat(list2.get(0).port().number()).isEqualTo(list1.get(0).port().number());

            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
        });
    }

    @Test
    @DisplayName("When two reusable and one non-reusable mongo container is requested - create two containers")
    void test_4() {
        runFixture(fixture -> {
            var reusableRequestedContainer1 = buildMongoContainerRequest(fixture, source1, true);
            var reusableRequest1 = prepareContainerRequest(
                    fixture.log,
                    reusableRequestedContainer1
            );
            var reusableId1 = reusableRequestedContainer1.id().json();
            var reusableRequestedContainer2 = buildMongoContainerRequest(fixture, source2, true);
            var reusableRequest2 = prepareContainerRequest(
                    fixture.log,
                    reusableRequestedContainer2
            );
            var reusableId2 = reusableRequestedContainer2.id().json();
            var nonReusableRequestedContainer = buildMongoContainerRequest(fixture, source3, false);
            var nonReusableRequest = prepareContainerRequest(
                    fixture.log,
                    nonReusableRequestedContainer
            );
            var nonReusableId = nonReusableRequestedContainer.id().json();

            // Start non-reusable container
            var list1 = fixture.application.containers(nonReusableRequest).start().list();
            assertThat(DockerUtil.findHuskitContainersWithId(nonReusableId)).hasSize(1);
            assertThat(list1).hasSize(1);
            assertThat(list1.get(0).id().json()).isEqualTo(nonReusableId);
            assertThat(list1.get(0).port().number()).isNotEqualTo(0);

            // Start first reusable container
            var list2 = fixture.application.containers(reusableRequest1).start().list();
            assertThat(DockerUtil.findHuskitContainersWithId(reusableId1)).hasSize(1);
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(reusableId1);
            assertThat(list2.get(0).port().number()).isNotEqualTo(list1.get(0).port().number());

            // Start second reusable container
            var list3 = fixture.application.containers(reusableRequest2).start().list();
            assertThat(DockerUtil.findHuskitContainersWithId(reusableId2)).hasSize(1);
            assertThat(DockerUtil.findHuskitContainersWithId(reusableId2)).hasSize(1);
            assertThat(DockerUtil.findHuskitContainersWithId(reusableId2)).hasSize(1);
            assertThat(list3).hasSize(1);
            assertThat(list3.get(0).id().json()).isEqualTo(reusableId2);
            assertThat(list3.get(0).port().number()).isNotEqualTo(list1.get(0).port().number()).isEqualTo(list2.get(0).port().number());
        });
    }

    @Test
    @DisplayName("When reusable")
    void test_5() {
        runFixture(fixture -> {
            var requestedContainer = buildMongoContainerRequest(fixture, source1, true);
            var containersRequest = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer)
            );
            var id = requestedContainer.id().json();

            var list1 = fixture.application.containers(containersRequest).start().list();
            assertThat(list1).hasSize(1);
            assertThat(list1.get(0).id().json()).isEqualTo(id);
            assertThat(list1.get(0).port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(containersRequest).start().list();
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(id);
            assertThat(list2.get(0).port().number()).isEqualTo(list1.get(0).port().number());

            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
        });
    }

    private DefaultMongoRequestedContainer buildMongoContainerRequest(Fixture fixture,
                                                                      String source,
                                                                      boolean isAllowedReuse) {
        return new DefaultMongoRequestedContainer(
                new DefaultRequestedContainer(
                        source,
                        MongoContainer.DEFAULT_IMAGE,
                        new MongoContainerId(
                                "",
                                source,
                                MongoContainer.DEFAULT_IMAGE,
                                "",
                                isAllowedReuse,
                                isAllowedReuse,
                                isAllowedReuse
                        ),
                        fixture.port,
                        ContainerType.MONGO,
                        new DefaultMongoContainerReuse(isAllowedReuse, isAllowedReuse, isAllowedReuse)
                ),
                databaseName
        );
    }
}
