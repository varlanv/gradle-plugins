package io.huskit.containers.integration.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import io.huskit.common.HtConstants;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import io.huskit.log.ProfileLog;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class HtMongoIntegrationTest implements DockerIntegrationTest {

    @Test
    @Disabled
    void mongo_test() {
        long var = System.currentTimeMillis();
        var subject = HtMongo.fromImage(HtConstants.Mongo.DEFAULT_IMAGE)
//            .withContainerSpec(spec -> spec.reuse().enabledWithCleanupAfter(Duration.ofMinutes(120)))
            .withContainerSpec(spec -> spec.reuse().disabled())
            .start();
        var connectionString = subject.connectionString();
        System.out.println("Mongo container create time - " + Duration.ofMillis(System.currentTimeMillis() - var));
        verifyMongoConnection(connectionString);

        assertThat(subject.connectionString()).isEqualTo(subject.connectionString());
        assertThat(subject.id()).isNotBlank();
        assertThat(subject.properties()).containsEntry(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV, subject.connectionString());
        assertThat(subject.properties()).containsEntry(HtConstants.Mongo.DEFAULT_DB_NAME_ENV, HtConstants.Mongo.DEFAULT_DB_NAME);
        assertThat(subject.properties()).containsKey(HtConstants.Mongo.DEFAULT_PORT_ENV);
        System.out.println("Mongo container create and verify time - " + Duration.ofMillis(System.currentTimeMillis() - var));
    }

    @Test
    @Disabled
    void testcontainers() {
        try (var mongoDBContainer = new MongoDBContainer(HtConstants.Mongo.DEFAULT_IMAGE)) {
//            var time = System.currentTimeMillis();
            mongoDBContainer.start();
//            System.out.println("Mongo container create time - " + Duration.ofMillis(System.currentTimeMillis() - time));
        }
        try (var mongoDBContainer = new MongoDBContainer(HtConstants.Mongo.DEFAULT_IMAGE)) {
            var time = System.currentTimeMillis();
            mongoDBContainer.start();
            System.out.println("Mongo container create time - " + Duration.ofMillis(System.currentTimeMillis() - time));
        }
    }

    @Test
    @Disabled
    void http_client() {
        var client = DockerClientFactory.instance().client();
        client.authCmd().exec();
        ProfileLog.withProfile("test", () -> {
            System.out.println(client.listContainersCmd().exec());
            System.out.println(client.listContainersCmd().exec());
        });
    }

    private void verifyMongoConnection(String connectionString) {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            var test = mongoClient.getDatabase("test");
            var testCollection = test.getCollection("test_collection");
            var objectId = new ObjectId();
            var key = "key";
            var value = "value";
            var insertOneResult = testCollection.insertOne(
                new Document()
                    .append("_id", objectId)
                    .append(key, value)
            );
            var doc = testCollection.find(Filters.eq("_id", insertOneResult.getInsertedId())).first();
            assertThat(objectId).isEqualTo(doc.get("_id"));
            assertThat(value).isEqualTo(doc.get(key));
        }
    }
}
