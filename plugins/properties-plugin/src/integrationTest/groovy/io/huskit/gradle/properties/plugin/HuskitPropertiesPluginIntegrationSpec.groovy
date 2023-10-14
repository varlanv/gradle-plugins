package io.huskit.gradle.properties.plugin

import io.huskit.gradle.common.plugin.model.props.Props
import io.huskit.gradle.common.plugin.model.props.fake.FakeProps
import io.huskit.gradle.commontest.BaseIntegrationSpec

class HuskitPropertiesPluginIntegrationSpec extends BaseIntegrationSpec {

    def "plugin should be applied"() {
        given:
        def project = setupProject()

        when:
        project.plugins.apply(HuskitPropertiesPlugin)

        then:
        project.plugins.hasPlugin(HuskitPropertiesPlugin)
    }

    def "should add props extension if not already exists"() {
        given:
        def project = setupProject()
        assert project.extensions.findByName(Props.EXTENSION_NAME) == null

        when:
        project.plugins.apply(HuskitPropertiesPlugin)

        then:
        project.extensions.findByName(Props.EXTENSION_NAME) != null
    }

    def "should use existing props extension if already exists"() {
        given:
        def project = setupProject()
        def fakeProps = new FakeProps()
        project.extensions.add(Props, Props.EXTENSION_NAME, fakeProps)

        when:
        project.plugins.apply(HuskitPropertiesPlugin)

        then:
        project.extensions.findByName(Props.EXTENSION_NAME) == fakeProps
    }
}
