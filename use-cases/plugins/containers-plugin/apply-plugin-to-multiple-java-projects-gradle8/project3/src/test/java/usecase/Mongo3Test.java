package usecase;

import org.junit.jupiter.api.RepeatedTest;

public class Mongo3Test {

    @RepeatedTest(1)
    public void someMethodTest() throws Exception {
        MongoUseCase.verifyMongoConnection();
    }
}
