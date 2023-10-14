package io.huskit.gradle.common.plugin.model.props

import io.huskit.gradle.commontest.BaseIntegrationSpec
import spock.lang.Shared

class DefaultNullablePropIntegrationSpec extends BaseIntegrationSpec {

    @Shared
    String propName = "anyPropName"
    String propVal = "anyPropVal"

    def "'name' should return prop name"() {
        given:
        def project = setupProject()
        def subject = new DefaultNullableProp(propName, project.providers.provider({ propVal }))

        expect:
        subject.name() == propName
    }

    def "'value' should return prop value"() {
        given:
        def project = setupProject()
        def subject = new DefaultNullableProp(propName, project.providers.provider({ propVal }))

        expect:
        subject.value() == propVal
    }

    def "'stringValue' should return prop value"() {
        given:
        def project = setupProject()
        def subject = new DefaultNullableProp(propName, project.providers.provider({ propVal }))

        expect:
        subject.stringValue() == propVal
    }

    def "'holdsTrue' should return true if prop value is true"() {
        given:
        def project = setupProject()
        def subject = new DefaultNullableProp(propName, project.providers.provider({ truth }))

        expect:
        subject.holdsTrue() == expected

        where:
        truth   | expected
        "tRuE"  | true
        "true"  | true
        "TRUE"  | true
        "false" | false
        null    | false
        ""      | false
    }

    def "'holdsFalse' should return true if prop value is false"() {
        given:
        def project = setupProject()
        def subject = new DefaultNullableProp(propName, project.providers.provider({ truth }))

        expect:
        subject.holdsFalse() == expected

        where:
        truth   | expected
        "false" | true
        "FALSE" | true
        "FaLsE" | true
        "true"  | false
        null    | false
        ""      | false
    }
}
