package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.id.ContainerId;
import io.huskit.gradle.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.util.function.Supplier;

@Value
@FieldNameConstants
@RequiredArgsConstructor
public class MongoContainerId implements ContainerId {

    private static final String JSON_TEMPLATE = "{" +
            "\"" + Fields.rootProjectName + "\":\"%s\"," +
            "\"" + Fields.imageName + "\":\"%s\"," +
            "\"" + Fields.databaseName + "\":\"%s\"," +
            "\"" + Fields.reuseBetweenBuilds + "\":%s," +
            "\"" + Fields.newDatabaseForEachTask + "\":%s" +
            "}";
    String rootProjectName;
    String imageName;
    String databaseName;
    boolean reuseBetweenBuilds;
    boolean newDatabaseForEachTask;
    Supplier<String> json = new MemoizedSupplier<>(this::_json);

    @Override
    public String json() {
        return json.get();
    }

    @Override
    public String toString() {
        return json();
    }

    private String _json() {
        return String.format(JSON_TEMPLATE,
                rootProjectName, imageName, databaseName, reuseBetweenBuilds, newDatabaseForEachTask);
    }
}
