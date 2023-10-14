package io.huskit.gradle.common.plugin.model.props

import io.huskit.gradle.common.plugin.model.props.fake.FakeProps
import io.huskit.gradle.common.plugin.model.props.fake.RandomizeIfEmptyProps
import io.huskit.gradle.commontest.BaseIntegrationSpec
import org.gradle.api.Project

class ReplaceablePropsIntegrationSpec extends BaseIntegrationSpec {

    def "'hasProp' should return true if existing extension has property"() {
        given:
        def project = setupProject()
        def subject = prepareSubjectAndExtension(project)

        expect:
        subject.hasProp("any") == true
    }

    def "'hasProp' should return false if existing extension has no property"() {
        given:
        def project = setupProject()
        def subject = prepareSubject()

        expect:
        subject.hasProp("any") == false
    }

    private ReplaceableProps prepareSubjectAndExtension(Project project) {
        project.extensions.add(Props, Props.EXTENSION_NAME, new RandomizeIfEmptyProps(new FakeProps()))
        return prepareSubject()
    }

    private ReplaceableProps prepareSubject() {
        def subject = new ReplaceableProps(project.providers, project.extensions)
        return subject
    }
}
