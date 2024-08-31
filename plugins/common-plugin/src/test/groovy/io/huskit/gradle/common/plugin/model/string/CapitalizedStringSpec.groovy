package io.huskit.gradle.common.plugin.model.string

import io.huskit.gradle.commontest.BaseUnitSpec

class CapitalizedStringSpec extends BaseUnitSpec {

    def "should capitalize the first letter of a string"() {
        expect:
        new CapitalizedString(expected).toString() == actual

        where:
        expected | actual
        "hello"  | "Hello"
        "123"    | "123"
        "HELLO"  | "HELLO"
        "hELLO"  | "HELLO"
        "Hello"  | "Hello"
    }

    def "null shouldn't be handled"() {
        when:
        new CapitalizedString(null)

        then:
        thrown(NullPointerException)
    }
}
