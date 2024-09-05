package io.huskit.containers.testcontainers.mongo;

import io.huskit.ContainersIntegrationTest;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.DefaultRequestedContainer;
import io.huskit.containers.model.id.MongoContainerId;
import io.huskit.containers.model.request.DefaultMongoRequestedContainer;
import io.huskit.containers.model.reuse.DefaultMongoContainerReuseOptions;
import io.huskit.gradle.commontest.DockerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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
            var requestedContainer = buildMongoContainerRequest(fixture, source1, new DefaultMongoContainerReuseOptions(true, false, false));
            var containersRequest = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer)
            );
            var id = requestedContainer.id().json();

            var list1 = fixture.application.containers(containersRequest).start().list();
            assertThat(list1).hasSize(1);
            var container1 = (MongoContainer) list1.get(0);
            assertThat(container1.id().json()).isEqualTo(id);
            assertThat(container1.port().number()).isNotEqualTo(0);
            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);

            var list2 = fixture.application.containers(containersRequest).start().list();
            assertThat(list2).hasSize(1);
            var container2 = (MongoContainer) list2.get(0);
            assertThat(container2.id().json()).isEqualTo(id);
            assertThat(container2.port().number()).isEqualTo(container1.port().number());
            assertThat(container2.connectionString()).isEqualTo(container1.connectionString()).isNotBlank();
            assertThat(container2.environment()).isEqualTo(container1.environment()).isNotEmpty();

            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
        });
    }

    @Test
    @DisplayName("When same reusable mongo container is requested twice with different sources - only one container is started")
    void test_1() {
        runFixture(fixture -> {
            var requestedContainer1 = buildMongoContainerRequest(fixture, source1, new DefaultMongoContainerReuseOptions(true, false, false));
            var containersRequest1 = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer1)
            );
            var id1 = requestedContainer1.id().json();
            var requestedContainer2 = buildMongoContainerRequest(fixture, source2, new DefaultMongoContainerReuseOptions(true, false, false));
            var containersRequest2 = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer2)
            );
            var id2 = requestedContainer2.id().json();

            var list1 = fixture.application.containers(containersRequest1).start().list();
            assertThat(list1).hasSize(1);
            var container1 = (MongoContainer) list1.get(0);
            assertThat(container1.id().json()).isEqualTo(id1);
            assertThat(container1.port().number()).isNotEqualTo(0);
            assertThat(DockerUtil.findHuskitContainersWithIds(id1, id2)).hasSize(1);

            var list2 = fixture.application.containers(containersRequest2).start().list();
            assertThat(list2).hasSize(1);
            var container2 = (MongoContainer) list2.get(0);
            assertThat(container2.id().json()).isEqualTo(id2);
            assertThat(container2.port().number()).isEqualTo(container1.port().number());
            assertThat(container1.connectionString()).isEqualTo(container2.connectionString()).isNotBlank();
            assertThat(container1.environment()).isEqualTo(container2.environment()).isNotEmpty();

            assertThat(DockerUtil.findHuskitContainersWithIds(id1, id2)).hasSize(1);
        });
    }

    @Test
    @DisplayName("When same non reusable mongo container is requested twice with different sources - create two containers")
    void test_2() {
        runFixture(fixture -> {
            var requestedContainer1 = buildMongoContainerRequest(fixture, source1, new DefaultMongoContainerReuseOptions(false));
            var request1 = prepareContainerRequest(
                    fixture.log,
                    requestedContainer1
            );
            var id1 = requestedContainer1.id().json();

            var requestedContainer2 = buildMongoContainerRequest(fixture, source2, new DefaultMongoContainerReuseOptions(false));
            var request2 = prepareContainerRequest(
                    fixture.log,
                    requestedContainer2
            );
            var id2 = requestedContainer2.id().json();

            var list1 = fixture.application.containers(request1).start().list();
            assertThat(DockerUtil.findHuskitContainersWithIds(id1, id2)).hasSize(1);
            assertThat(list1).hasSize(1);
            var container1 = (MongoContainer) list1.get(0);
            assertThat(container1.id().json()).isEqualTo(id1);
            assertThat(container1.port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(request2).start().list();
            assertThat(list2).hasSize(1);
            var container2 = (MongoContainer) list2.get(0);
            assertThat(container2.id().json()).isEqualTo(id2);
            assertThat(container2.port().number()).isNotEqualTo(container1.port().number());
            assertThat(container2.connectionString()).isNotEqualTo(container1.connectionString()).isNotBlank();
            assertThat(container2.environment()).isNotEqualTo(container1.environment()).isNotEmpty();
            assertThat(DockerUtil.findHuskitContainersWithIds(id1, id2)).hasSize(2);
        });
    }

    @Test
    @DisplayName("When same non reusable mongo container is requested twice with same sources - create one container")
    void test_3() {
        runFixture(fixture -> {
            var requestedContainer = buildMongoContainerRequest(fixture, source1, new DefaultMongoContainerReuseOptions(false));
            var request = prepareContainerRequest(
                    fixture.log,
                    requestedContainer
            );
            var id = requestedContainer.id().json();

            var list1 = fixture.application.containers(request).start().list();
            assertThat(list1).hasSize(1);
            var container1 = (MongoContainer) list1.get(0);
            assertThat(container1.id().json()).isEqualTo(id);
            assertThat(container1.port().number()).isNotEqualTo(0);
            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);

            var list2 = fixture.application.containers(request).start().list();
            assertThat(list2).hasSize(1);
            var container2 = (MongoContainer) list2.get(0);
            assertThat(container2.id().json()).isEqualTo(id);
            assertThat(container2.port().number()).isEqualTo(container1.port().number());
            assertThat(container1.connectionString()).isEqualTo(container2.connectionString()).isNotBlank();
            assertThat(container1.environment()).isEqualTo(container2.environment()).isNotEmpty();
            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
        });
    }

    @Test
    @DisplayName("When two reusable and one non-reusable mongo container is requested - create two containers")
    void test_4() {
        runFixture(fixture -> {
            var reusableRequestedContainer1 = buildMongoContainerRequest(fixture, source1, new DefaultMongoContainerReuseOptions(true));
            var reusableRequest1 = prepareContainerRequest(
                    fixture.log,
                    reusableRequestedContainer1
            );
            var reusableId1 = reusableRequestedContainer1.id().json();
            var reusableRequestedContainer2 = buildMongoContainerRequest(fixture, source2, new DefaultMongoContainerReuseOptions(true));
            var reusableRequest2 = prepareContainerRequest(
                    fixture.log,
                    reusableRequestedContainer2
            );
            var reusableId2 = reusableRequestedContainer2.id().json();
            var nonReusableRequestedContainer = buildMongoContainerRequest(fixture, source3, new DefaultMongoContainerReuseOptions(false));
            var nonReusableRequest = prepareContainerRequest(
                    fixture.log,
                    nonReusableRequestedContainer
            );
            var nonReusableId = nonReusableRequestedContainer.id().json();

            // Start non-reusable container
            var nonReusableContainerList = fixture.application.containers(nonReusableRequest).start().list();
            assertThat(DockerUtil.findHuskitContainersWithIds(nonReusableId, reusableId1, reusableId2)).hasSize(1);
            assertThat(nonReusableContainerList).hasSize(1);
            var nonReusableContainer = (MongoContainer) nonReusableContainerList.get(0);
            assertThat(nonReusableContainer.id().json()).isEqualTo(nonReusableId);
            assertThat(nonReusableContainer.port().number()).isNotEqualTo(0);

            // Start first reusable container
            var reusableContainerList1 = fixture.application.containers(reusableRequest1).start().list();
            assertThat(DockerUtil.findHuskitContainersWithIds(nonReusableId, reusableId1, reusableId2)).hasSize(2);
            assertThat(reusableContainerList1).hasSize(1);
            var reusableContainer1 = (MongoContainer) reusableContainerList1.get(0);
            assertThat(reusableContainer1.id().json()).isEqualTo(reusableId1);
            assertThat(reusableContainer1.port().number()).isNotEqualTo(nonReusableContainer.port().number());

            // Start second reusable container
            var reusableContainerList2 = fixture.application.containers(reusableRequest2).start().list();
            assertThat(DockerUtil.findHuskitContainersWithIds(nonReusableId, reusableId1, reusableId2)).hasSize(2);
            assertThat(reusableContainerList2).hasSize(1);
            var reusableContainer2 = (MongoContainer) reusableContainerList2.get(0);
            assertThat(reusableContainer2.id().json()).isEqualTo(reusableId2);
            assertThat(reusableContainer2.port().number()).isNotEqualTo(nonReusableContainer.port().number()).isEqualTo(reusableContainer1.port().number());
            assertThat(reusableContainer2.connectionString()).isNotEqualTo(reusableContainer1.connectionString()).isNotBlank();
            assertThat(reusableContainer2.environment()).isNotEqualTo(reusableContainer1.environment()).isNotEmpty();
            assertThat(reusableContainer2.environment()).isNotEqualTo(nonReusableContainer.environment());
            assertThat(reusableContainer2.connectionString()).isNotEqualTo(nonReusableContainer.connectionString());
        });
    }

    @Test
    @DisplayName("When reusable `newDatabaseForEachRequest` and `dontStopOnClose` reuse options set to true, but `enabled` to false - ignore reuse options")
    void test_5() {
        runFixture(fixture -> {
            var reuseOptions = new DefaultMongoContainerReuseOptions(false, true, true);
            var requestedContainer = buildMongoContainerRequest(fixture, source1, reuseOptions);
            var containersRequest = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer)
            );
            var id = requestedContainer.id().json();

            var list1 = fixture.application.containers(containersRequest).start().list();
            assertThat(list1).hasSize(1);
            var container1 = (MongoContainer) list1.get(0);
            assertThat(container1.id().json()).isEqualTo(id);
            assertThat(container1.port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(containersRequest).start().list();
            assertThat(list2).hasSize(1);
            var container2 = (MongoContainer) list2.get(0);
            assertThat(container2.id().json()).isEqualTo(id);
            assertThat(container2.port().number()).isEqualTo(container1.port().number());
            assertThat(container1.connectionString()).isNotEqualTo(container2.connectionString()).isNotBlank();

            assertThat(DockerUtil.findHuskitContainersWithId(id)).hasSize(1);
        });
    }

    @Test
    @DisplayName("When reusable `newDatabaseForEachRequest` is set to true, calling `connectionString` multiple times should give different strings")
    void test_6() {
        runFixture(fixture -> {
            var reuseOptions = new DefaultMongoContainerReuseOptions(true, true, false);
            var requestedContainer = buildMongoContainerRequest(fixture, source1, reuseOptions);
            var containersRequest = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer)
            );
            var id = requestedContainer.id().json();

            var list1 = fixture.application.containers(containersRequest).start().list();
            assertThat(list1).hasSize(1);
            var container = (MongoContainer) list1.get(0);
            assertThat(container.id().json()).isEqualTo(id);
            assertThat(container.port().number()).isNotEqualTo(0);
            assertThat(container.connectionString()).isNotBlank().isNotEqualTo(container.connectionString());
        });
    }

    private DefaultMongoRequestedContainer buildMongoContainerRequest(Fixture fixture,
                                                                      String source,
                                                                      DefaultMongoContainerReuseOptions reuse) {
        return new DefaultMongoRequestedContainer(
                new DefaultRequestedContainer(
                        source,
                        MongoContainer.DEFAULT_IMAGE,
                        new MongoContainerId(
                                "",
                                source,
                                MongoContainer.DEFAULT_IMAGE,
                                "",
                                reuse.dontStopOnClose(),
                                reuse.newDatabaseForEachRequest(),
                                reuse.enabled()
                        ),
                        fixture.port,
                        ContainerType.MONGO,
                        reuse
                ),
                databaseName
        );
    }
}
