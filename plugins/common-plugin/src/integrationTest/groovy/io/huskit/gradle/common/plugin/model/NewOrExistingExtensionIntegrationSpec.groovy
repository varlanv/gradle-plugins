package io.huskit.gradle.common.plugin.model

import io.huskit.gradle.common.plugin.model.props.Props
import io.huskit.gradle.common.plugin.model.props.fake.FakeProps
import io.huskit.gradle.commontest.BaseIntegrationSpec

class NewOrExistingExtensionIntegrationSpec extends BaseIntegrationSpec {

    def "if extension not exists, then should create"() {
        given:
        def project = setupProject()
        def subject = new NewOrExistingExtension(project.extensions)

        when:
        def actual = subject.getOrCreate(Props, Props.EXTENSION_NAME, { new FakeProps() })

        then:
        actual != null
        project.extensions.findByName(Props.EXTENSION_NAME) === actual
    }

    def "if extension exists, then should return existing"() {
        given:
        def project = setupProject()
        def subject = new NewOrExistingExtension(project.extensions)
        def expected = new FakeProps()
        project.extensions.add(Props.EXTENSION_NAME, expected)

        when:
        def actual = subject.getOrCreate(Props, Props.EXTENSION_NAME, { new FakeProps() })

        then:
        actual === expected
    }
}
