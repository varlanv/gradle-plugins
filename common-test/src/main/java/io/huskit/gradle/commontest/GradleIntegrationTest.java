package io.huskit.gradle.commontest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.testcontainers.shaded.org.apache.commons.io.FileDeleteStrategy;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GradleIntegrationTest extends IntegrationTest {

    default void useProjectFixture(ThrowableConsumer<SingleProjectFixture> fixtureConsumer) {
        useProjectFixture(
                ProjectBuilder::build,
                fixtureConsumer
        );
    }

    @SneakyThrows
    default void useProjectFixture(ThrowableConsumer<File> projectDirConsumer,
                                   ThrowableConsumer<SingleProjectFixture> fixtureConsumer) {
        useProjectFixture(
                projectDirConsumer,
                ProjectBuilder::build, fixtureConsumer
        );
    }

    @SneakyThrows
    default void useProjectFixture(Function<ProjectBuilder, Project> projectBuilderFn,
                                   ThrowableConsumer<SingleProjectFixture> fixtureConsumer) {
        useProjectFixture(
                pb -> {
                },
                projectBuilderFn,
                fixtureConsumer);
    }

    @SneakyThrows
    default void useProjectFixture(ThrowableConsumer<File> projectDirConsumer,
                                   Function<ProjectBuilder, Project> projectBuilderFn,
                                   ThrowableConsumer<SingleProjectFixture> fixtureConsumer) {
        var projectDir = newTempDir();
        RuntimeException originalException = null;
        try {
            projectDirConsumer.accept(projectDir);
            var projectBuilder = ProjectBuilder.builder()
                    .withProjectDir(projectDir)
                    .withGradleUserHomeDir(projectDir);
            var project = projectBuilderFn.apply(projectBuilder);
            fixtureConsumer.accept(new SingleProjectFixture(projectDir, project));
        } catch (RuntimeException e) {
            originalException = e;
        } finally {
            try {
//                FileDeleteStrategy.FORCE.delete(projectDir);
                boolean delete = projectDir.delete();
                System.out.println();
            } catch (RuntimeException e) {
                throw new RuntimeException(Objects.requireNonNullElse(originalException, e));
            }
        }
    }

    @SneakyThrows
    default void useProjectWithParent(Consumer<ProjectWithParentFixture> fixtureConsumer) {
        var parentProjectDirectory = newTempDir();
        RuntimeException originalException = null;
        try {
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
        } catch (RuntimeException e) {
            originalException = e;
        } finally {
            try {
                parentProjectDirectory.delete();
            } catch (RuntimeException e) {
                throw new RuntimeException(Objects.requireNonNullElse(originalException, e));
            }
        }
    }

    @SneakyThrows
    private File newTempDir() {
        var dir = Files.createTempDirectory("pepega-").toFile();
//        dir.deleteOnExit();
        return dir;
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


    default void evaluateProject(Project project) {
        ((ProjectInternal) project).evaluate();
    }
}
