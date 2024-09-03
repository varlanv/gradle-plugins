package io.huskit.gradle.commontest;

import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Function;

@Tag("integration-test")
public class BaseIntegrationTest extends BaseTest {

    @TempDir
    protected @NonFinal File projectDir;
    protected @NonFinal Project project;
    protected @NonFinal ObjectFactory objects;
    protected @NonFinal ProviderFactory providers;

    protected Project setupProject() {
        return setupProject(ProjectBuilder::build);
    }

    protected Project setupProject(Consumer<File> projectDirConsumer) {
        projectDirConsumer.accept(projectDir);
        return setupProject();
    }

    protected Project setupProject(Consumer<File> projectDirConsumer, Function<ProjectBuilder, Project> projectBuilderFn) {
        projectDirConsumer.accept(projectDir);
        return setupProject(projectBuilderFn);
    }

    protected Project setupProject(Function<ProjectBuilder, Project> projectBuilderFn) {
        var projectBuilder = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withGradleUserHomeDir(projectDir);
        var project = projectBuilderFn.apply(projectBuilder);
        this.objects = project.getObjects();
        this.providers = project.getProviders();
        this.project = project;
        return project;
    }

    @SneakyThrows
    protected ProjectWithParentFixture setupFixtureWithParentProject() {
        var parentProjectDirectory = Files.createTempDirectory("gradle_test").toFile();
        var projectDirectory = Files.createTempDirectory(parentProjectDirectory.toPath(), "gradle_test").toFile();
        var parentProject = ProjectBuilder.builder().withProjectDir(parentProjectDirectory).build();
        var project = ProjectBuilder.builder().withProjectDir(projectDirectory).withParent(parentProject).build();
        return new ProjectWithParentFixture(
                project,
                parentProject,
                projectDirectory,
                parentProjectDirectory,
                project.getObjects(),
                project.getProviders()
        );
    }

    protected void evaluateProject() {
        evaluateProject(project);
    }

    protected void evaluateProject(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}
