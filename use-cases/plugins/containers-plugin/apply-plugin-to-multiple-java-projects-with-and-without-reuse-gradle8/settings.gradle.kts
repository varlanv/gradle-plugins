rootProject.name = "apply-plugin-to-multiple-java-projects-with-and-without-reuse-gradle8"

fun pathToRoot(dir: File, parts: MutableList<String> = mutableListOf()): String {
    val targetFolderName = "internal-convention-plugin"
    return when {
        dir.resolve(targetFolderName).exists() -> parts.joinToString("") { "../" }
        dir.parentFile != null -> pathToRoot(dir.parentFile, parts.apply { add(dir.name) })
        else -> throw IllegalStateException("Cannot find a directory containing $targetFolderName")
    }
}

val huskitRootProjectDir = pathToRoot(rootProject.projectDir)

includeBuild(huskitRootProjectDir + "/use-cases/common-use-cases-logic/common-containers-plugin-use-case-logic") {
    dependencySubstitution {
        substitute(module("plugin-usecases:usecases")).using(project(":"))
    }
}

include(
        "project1",
        "project2",
        "project3",
)

if (!providers.environmentVariable("FUNCTIONAL_SPEC_RUN").isPresent) {
    includeBuild(huskitRootProjectDir)
}
