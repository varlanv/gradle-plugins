package io.huskit.gradle.common.plugin.model.props;

import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import io.huskit.gradle.common.plugin.model.props.fake.FakeProps;
import io.huskit.gradle.common.plugin.model.props.fake.RandomizeIfEmptyProps;
import io.huskit.gradle.commontest.BaseGradleIntegrationTest;
import io.huskit.log.Log;
import io.huskit.log.fake.FakeLog;
import org.gradle.api.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReplaceablePropsIntegrationTest extends BaseGradleIntegrationTest {

    @Test
    @DisplayName("'hasProp' should return true if existing extension has property")
    void test_0() {
        var project = setupProject();
        var log = new FakeLog();
        var subject = prepareSubjectAndExtension(project, log);

        assertThat(subject.hasProp("any")).isTrue();

        assertThat(log.loggedMessages()).hasSize(1);
        var loggedMessage = log.loggedMessages().get(0);
        assertThat(loggedMessage.args()).hasSize(1);
        assertThat(loggedMessage.args().get(0)).isEqualTo(Props.EXTENSION_NAME);
        assertThat(loggedMessage.message()).contains("found, using existing instance");
    }

    @Test
    @DisplayName("'hasProp' should return false if existing extension has no property")
    void test_1() {
        var project = setupProject();
        var log = new FakeLog();
        var subject = prepareSubject(log);

        assertThat(subject.hasProp("any")).isFalse();

        assertThat(log.loggedMessages()).hasSize(1);
        var loggedMessage = log.loggedMessages().get(0);
        assertThat(loggedMessage.args()).hasSize(1);
        assertThat(loggedMessage.args().get(0)).isEqualTo(Props.EXTENSION_NAME);
        assertThat(loggedMessage.message()).contains("not found, creating new instance");
    }

    private ReplaceableProps prepareSubjectAndExtension(Project project, Log log) {
        project.getExtensions().add(Props.class, Props.EXTENSION_NAME, new RandomizeIfEmptyProps(new FakeProps()));
        return prepareSubject(log);
    }

    private ReplaceableProps prepareSubject(Log log) {
        return new ReplaceableProps(
                project.getProviders(),
                project.getExtensions().getExtraProperties(),
                new NewOrExistingExtension(
                        log,
                        project.getExtensions()
                )
        );
    }
}
