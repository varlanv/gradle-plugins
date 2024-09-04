package io.huskit.containers.testcontainers.mongo;

import io.huskit.BaseContainersIntegrationTest;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.DefaultRequestedContainer;
import io.huskit.containers.model.request.DefaultMongoRequestedContainer;
import io.huskit.containers.model.reuse.DefaultMongoContainerReuse;
import io.huskit.gradle.commontest.DockerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoContainerIntegrationTest extends BaseContainersIntegrationTest {

    String databaseName = "someDatabaseName";

    @Test
    @DisplayName("When same reusable mongo container is requested twice - only one container is started")
    void test_0() {
        test(fixture -> {
            var requestedContainer = buildMongoContainerRequest(fixture, fixture.id, true);
            var containersRequest = prepareContainerRequest(
                    fixture.log,
                    List.of(requestedContainer)
            );

            var list1 = fixture.application.containers(containersRequest).start().list();
            assertThat(list1).hasSize(1);
            assertThat(list1.get(0).id().json()).isEqualTo(fixture.id);
            assertThat(list1.get(0).port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(containersRequest).start().list();
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(fixture.id);
            assertThat(list2.get(0).port().number()).isEqualTo(list1.get(0).port().number());

            var huskitContainersWithId = DockerUtil.findHuskitContainersWithId(fixture.id);
            assertThat(huskitContainersWithId).hasSize(1);
        });
    }

    @Test
    @DisplayName("When same non reusable mongo container is requested twice - create two containers")
    void test_1() {
        test(fixture -> {
            var containersRequest = prepareContainerRequest(
                    fixture.log,
                    buildMongoContainerRequest(fixture, fixture.id, false));

            var list = fixture.application.containers(containersRequest).start().list();
            assertThat(list).hasSize(1);
            assertThat(list.get(0).id().json()).isEqualTo(fixture.id);
            assertThat(list.get(0).port().number()).isNotEqualTo(0);

            var list2 = fixture.application.containers(containersRequest).start().list();
            assertThat(list2).hasSize(1);
            assertThat(list2.get(0).id().json()).isEqualTo(fixture.id);
            assertThat(list2.get(0).port().number()).isEqualTo(list.get(0).port().number());

            var huskitContainersWithId = DockerUtil.findHuskitContainersWithId(fixture.id);
            assertThat(huskitContainersWithId).hasSize(1);
        });
    }

    private DefaultMongoRequestedContainer buildMongoContainerRequest(Fixture fixture, String id, boolean isAllowedReuse) {
        return new DefaultMongoRequestedContainer(
                new DefaultRequestedContainer(
                        fixture.source,
                        MongoContainer.DEFAULT_IMAGE,
                        id,
                        fixture.port,
                        ContainerType.MONGO,
                        new DefaultMongoContainerReuse(isAllowedReuse, isAllowedReuse, isAllowedReuse)
                ),
                databaseName
        );
    }
}
