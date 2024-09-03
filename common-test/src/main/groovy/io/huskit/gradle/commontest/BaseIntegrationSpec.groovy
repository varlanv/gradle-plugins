package io.huskit.gradle.commontest

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ProviderFactory
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Tag
import spock.lang.TempDir

import java.nio.file.Files
import java.util.function.Consumer

@Tag("integration-test")
abstract class BaseIntegrationSpec extends BaseSpec {

    @TempDir
    File projectDir
    Project project
    ObjectFactory objects
    ProviderFactory providers

    @CompileStatic
    protected Project setupProject(Consumer<File> projectDirClosure, @DelegatesTo(ProjectBuilder) Closure<Project> projectBuilderClosure = null) {
        projectDirClosure(projectDir)
        return setupProject(projectBuilderClosure)
    }

    @CompileStatic
    protected Project setupProject(@DelegatesTo(ProjectBuilder) Closure<Project> projectBuilderClosure = null) {
        ProjectBuilder projectBuilder = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withGradleUserHomeDir(projectDir)
        Project project
        if (projectBuilderClosure != null) {
            projectBuilderClosure.delegate = projectBuilder
            projectBuilderClosure.resolveStrategy = Closure.DELEGATE_FIRST
            project = projectBuilderClosure.call()
        } else {
            project = projectBuilder.build()
        }
        this.objects = project.objects
        this.providers = project.providers
        this.project = project
        return project
    }

    @CompileStatic
    protected ProjectWithParentFixture setupFixtureWithParentProject() {
        def parentProjectDirectory = Files.createTempDirectory("gradle_test").toFile()
        def projectDirectory = Files.createTempDirectory(parentProjectDirectory.toPath(), "gradle_test").toFile()
        def parentProject = ProjectBuilder.builder().withProjectDir(parentProjectDirectory).build()
        def project = ProjectBuilder.builder().withProjectDir(projectDirectory).withParent(parentProject).build()
        return new ProjectWithParentFixture(
                project,
                parentProject,
                projectDirectory,
                parentProjectDirectory,
                project.objects,
                project.providers
        )
    }

    @CompileStatic
    protected void evaluateProject() {
        evaluateProject(project)
    }

    @CompileStatic
    protected void evaluateProject(Project project) {
        (project as ProjectInternal).evaluate()
    }
}
