package io.huskit.gradle.aspectj.plugin

import io.huskit.gradle.commontest.BaseIntegrationSpec
import org.gradle.api.Project

class HuskitAspectJPluginSpec extends BaseIntegrationSpec {

    def "plugin should be applied"() {
        given:
        Project project = setupProject()

        when:
        project.plugins.apply(HuskitAspectJPlugin)

        then:
        project.plugins.hasPlugin(HuskitAspectJPlugin)
    }
}
