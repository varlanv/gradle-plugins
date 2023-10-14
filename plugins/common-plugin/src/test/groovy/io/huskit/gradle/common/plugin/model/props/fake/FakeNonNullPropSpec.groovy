package io.huskit.gradle.common.plugin.model.props.fake

import io.huskit.gradle.common.plugin.model.props.exception.NonNullPropertyException
import io.huskit.gradle.commontest.BaseUnitSpec
import spock.lang.Shared

class FakeNonNullPropSpec extends BaseUnitSpec {

    @Shared
    String propName = "propName"
    String propValue = "propVal"

    def "'name' should return param"() {
        expect:
        new FakeNonNullProp(propName, propValue).name() == propName
    }

    def "'value' if value non null should return param"() {
        expect:
        new FakeNonNullProp(propName, propValue).value() == propValue
    }

    def "'stringValue' if value non null should return param"() {
        expect:
        new FakeNonNullProp(propName, propValue).stringValue() == propValue
    }

    def "'value' if value null then throw exception"() {
        when:
        new FakeNonNullProp(propName, null).value()

        then:
        def exception = thrown(NonNullPropertyException)
        exception.message.contains(propName)
    }

    def "'stringValue' if value null then throw exception"() {
        when:
        new FakeNonNullProp(propName, null).stringValue()

        then:
        def exception = thrown(NonNullPropertyException)
        exception.message.contains(propName)
    }
}
