package io.huskit.gradle.commontest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GradleIntegrationTest extends IntegrationTest {

    default void runProjectFixture(ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        runProjectFixture(
                ProjectBuilder::build,
                fixtureConsumer
        );
    }

    @SneakyThrows
    default void runProjectFixture(ThrowingConsumer<File> projectDirConsumer,
                                   ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        runProjectFixture(
                projectDirConsumer,
                ProjectBuilder::build, fixtureConsumer
        );
    }

    @SneakyThrows
    default void runProjectFixture(Function<ProjectBuilder, Project> projectBuilderFn,
                                   ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        runProjectFixture(
                pb -> {
                },
                projectBuilderFn,
                fixtureConsumer);
    }

    default void runProjectFixture(ThrowingConsumer<File> projectDirConsumer,
                                   Function<ProjectBuilder, Project> projectBuilderFn,
                                   ThrowingConsumer<SingleProjectFixture> fixtureConsumer) {
        var projectDir = newTempDir();
        runAndDeleteFile(projectDir, () -> {
            projectDirConsumer.accept(projectDir);
            var projectBuilder = ProjectBuilder.builder()
                    .withProjectDir(projectDir)
                    .withGradleUserHomeDir(projectDir);
            var project = projectBuilderFn.apply(projectBuilder);
            fixtureConsumer.accept(new SingleProjectFixture(projectDir, project));
        });
    }

    @SneakyThrows
    default void useProjectWithParent(Consumer<ProjectWithParentFixture> fixtureConsumer) {
        var parentProjectDirectory = newTempDir();
        runAndDeleteFile(parentProjectDirectory, () -> {
            var projectDirectory = new File(parentProjectDirectory, "gradle_test");
            projectDirectory.mkdir();
            var parentProject = ProjectBuilder.builder().withProjectDir(parentProjectDirectory).build();
            var project = ProjectBuilder.builder().withProjectDir(projectDirectory).withParent(parentProject).build();
            fixtureConsumer.accept(
                    new ProjectWithParentFixture(
                            project,
                            parentProject,
                            projectDirectory,
                            parentProjectDirectory,
                            project.getObjects(),
                            project.getProviders()
                    )
            );
        });
    }

    default void evaluateProject(Project project) {
        ((ProjectInternal) project).evaluate();
    }

    @Getter
    @RequiredArgsConstructor
    class SingleProjectFixture {

        File projectDir;
        Project project;
        ObjectFactory objects;
        ProviderFactory providers;

        private SingleProjectFixture(File projectDir, Project project) {
            this(projectDir, project, project.getObjects(), project.getProviders());
        }
    }

    @Getter
    @RequiredArgsConstructor
    class ProjectWithParentFixture {

        Project project;
        Project parentProject;
        File projectDir;
        File parentProjectDir;
        ObjectFactory objects;
        ProviderFactory providers;

    }
}
