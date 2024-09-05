package io.huskit.containers.model.id;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.NonFinal;

@FieldNameConstants
@RequiredArgsConstructor
public final class MongoContainerId implements ContainerId {

    private static final String JSON_TEMPLATE = "{" +
            "\"" + Fields.rootProjectName + "\":\"%s\"," +
            "\"" + Fields.projectName + "\":\"%s\"," +
            "\"" + Fields.imageName + "\":\"%s\"," +
            "\"" + Fields.databaseName + "\":\"%s\"," +
            "\"" + Fields.reuseBetweenBuilds + "\":%s," +
            "\"" + Fields.newDatabaseForEachTask + "\":%s" +
            "}";
    String rootProjectName;
    String projectName;
    String imageName;
    String databaseName;
    boolean reuseBetweenBuilds;
    boolean newDatabaseForEachTask;
    transient boolean reuseEnabled;
    volatile transient @NonFinal String json;

    @Override
    public String json() {
        var result = json;
        if (result == null) {
            result = String.format(JSON_TEMPLATE,
                    rootProjectName, reuseEnabled ? "" : projectName, imageName, databaseName, reuseBetweenBuilds, newDatabaseForEachTask);
            json = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return json();
    }
}
