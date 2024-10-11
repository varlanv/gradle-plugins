package usecase;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.util.Objects;

public class MongoUseCase {

    public static void verifyMongoConnection() {
        var timeBefore = System.currentTimeMillis();
        String mongoConnectionStringEnv = "MONGO_CONNECTION_STRING";
        String mongoConnectionString = System.getenv(mongoConnectionStringEnv);
        try (MongoClient mongoClient = MongoClients.create(System.getenv(mongoConnectionStringEnv))) {
            MongoDatabase test = mongoClient.getDatabase("test");
            MongoCollection<Document> testCollection = test.getCollection("test_collection");
            ObjectId objectId = new ObjectId();
            String key = "key";
            String value = "value";
            InsertOneResult insertOneResult = testCollection.insertOne(
                    new Document()
                            .append("_id", objectId)
                            .append(key, value)
            );
            Document doc = testCollection.find(Filters.eq("_id", insertOneResult.getInsertedId())).first();
            if (!Objects.equals(objectId, doc.get("_id"))) {
                throw new RuntimeException();
            }
            if (!Objects.equals(value, doc.get(key))) {
                throw new RuntimeException();
            }
            String messageMarker = "~_~~";
            System.out.println(messageMarker + mongoConnectionStringEnv + "=" + mongoConnectionString);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            System.out.printf("Mongo test body time: [%s]%n", Duration.ofMillis(System.currentTimeMillis() - timeBefore));
        }
    }
}
