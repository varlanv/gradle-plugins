package io.huskit.gradle.containers.plugin.internal


import io.huskit.gradle.commontest.BaseUnitSpec

class MongoContainerIdSpec extends BaseUnitSpec {

    String rootProjectName = "rootProjectName"
    String imageName = "imageName"
    String databaseName = "databaseName"
    boolean reuseBetweenBuilds = true
    boolean newDatabaseForEachTask = true
    MongoContainerId subject = new MongoContainerId(rootProjectName, imageName, databaseName, reuseBetweenBuilds, newDatabaseForEachTask)
}
