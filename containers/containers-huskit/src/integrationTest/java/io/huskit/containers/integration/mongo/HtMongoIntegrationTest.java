package io.huskit.containers.integration.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Filters;
import io.huskit.containers.model.HtConstants;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import io.huskit.log.ProfileLog;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class HtMongoIntegrationTest implements DockerIntegrationTest {

    @Test
    void mongo_test() {
        var subject = HtMongo.fromImage(HtConstants.Mongo.DEFAULT_IMAGE)
                .withContainerSpec(spec -> spec.reuse().enabledWithCleanupAfter(Duration.ofMinutes(120)))
                .start();

        {
            var connectionString = subject.connectionString();
            verifyMongoConnection(connectionString);
        }

        {
            assertThat(subject.connectionString()).isEqualTo(subject.connectionString());
            assertThat(subject.id()).isNotBlank();
            assertThat(subject.properties()).containsEntry(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV, subject.connectionString());
            assertThat(subject.properties()).containsEntry(HtConstants.Mongo.DEFAULT_DB_NAME_ENV, HtConstants.Mongo.DEFAULT_DB_NAME);
            assertThat(subject.properties()).containsKey(HtConstants.Mongo.DEFAULT_PORT_ENV);
        }
    }

    @Test
    @Disabled
    void http_client() {
        var executor = Executors.newFixedThreadPool(1);
        final var before = System.currentTimeMillis();
        var client = DockerClientFactory.instance().client();
        client.authCmd().exec();
        ProfileLog.withProfile("test", () -> {
            System.out.println(client.listContainersCmd().exec());
            System.out.println(client.listContainersCmd().exec());
        });
    }

    @Test
    @Disabled
    @SneakyThrows
    void npipe() {
        System.loadLibrary("npipe");
        HttpClient client = HttpClient.newHttpClient();
        var uri = URI.create("npipe:////./pipe/docker_engine");
        // Create a URI for the Docker API using named pipes
        URI dockerUri = new URI("http://localhost:2375/v1.41/containers/json");

        // Create an HTTP request to Docker's API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(dockerUri)
                .GET()
                .build();

        // Send the request and retrieve the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Output the response
        System.out.println("Response code: " + response.statusCode());
        System.out.println("Response body: " + response.body());

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
