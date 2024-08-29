fun pathToRoot(dir: File, parts: MutableList<String> = mutableListOf()): String {
    val targetFolderName = "internal-convention-plugin"
    return when {
        dir.resolve(targetFolderName).exists() -> parts.joinToString("") { "../" }
        dir.parentFile != null -> pathToRoot(dir.parentFile, parts.apply { add(dir.name) })
        else -> throw IllegalStateException("Cannot find a directory containing $targetFolderName")
    }
}

include(
        "project1",
        "project2",
        "project3",
        "mongo-logic"
)

if (!providers.environmentVariable("FUNCTIONAL_SPEC_RUN").isPresent) {
    includeBuild(pathToRoot(rootProject.projectDir))
}
