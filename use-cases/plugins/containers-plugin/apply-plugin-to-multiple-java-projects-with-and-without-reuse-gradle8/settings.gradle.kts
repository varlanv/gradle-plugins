import java.io.File

rootProject.name = "apply-plugin-to-multiple-java-projects-with-and-without-reuse-gradle8"

fun pathToRoot(dir: File, parts: MutableList<String> = mutableListOf()): String {
    val targetFolderName = "internal-convention-plugin"
    return when {
        dir.resolve(targetFolderName).exists() -> parts.joinToString("") { "../" }
        dir.parentFile != null -> pathToRoot(dir.parentFile, parts.apply { add(dir.name) })
        else -> throw IllegalStateException("Cannot find a directory containing $targetFolderName")
    }
}

val useCasesDir = when {
    providers.provider({ System.getenv("FUNCTIONAL_SPEC_RUN") }).isPresent -> "../common-containers-plugin-use-case-logic"
    else -> pathToRoot(rootProject.projectDir) + "/use-cases/common-use-cases-logic/common-containers-plugin-use-case-logic"
}

includeBuild(useCasesDir) {
    dependencySubstitution {
        substitute(module("plugin-usecases:usecases")).using(project(":"))
    }
}

include(
        "project1",
        "project2",
        "project3"
)

if (!providers.provider({ System.getenv("FUNCTIONAL_SPEC_RUN") }).isPresent) {
    includeBuild(pathToRoot(rootProject.projectDir))
}
