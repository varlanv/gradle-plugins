package io.huskit.gradle.common.plugin.model.props.fake

import io.huskit.gradle.commontest.BaseUnitSpec
import spock.lang.Shared

class RandomizeIfEmptyPropsSpec extends BaseUnitSpec {

    @Shared
    String nonExistentPropName = "nonexistent"
    @Shared
    String existingPropName = "existing"
    @Shared
    String existingPropValue = "existingValue"

    def "if non null property not exists, then should return random value"() {
        given:
        def fakeProps = new FakeProps()
        def subject = new RandomizeIfEmptyProps(fakeProps)

        when:
        def actual = subject.nonnull(nonExistentPropName)

        then:
        actual != null
        actual.name() == nonExistentPropName
        actual.value() != null
        actual.stringValue() != null
    }

    def "if nullable property not exists, then should return random value"() {
        given:
        def fakeProps = new FakeProps()
        def subject = new RandomizeIfEmptyProps(fakeProps)

        when:
        def actual = subject.nullable(nonExistentPropName)

        then:
        actual != null
        actual.name() == nonExistentPropName
        actual.value() != null
        actual.stringValue() != null
    }

    def "if system environment property not exists, then should return random value"() {
        given:
        def fakeProps = new FakeProps()
        def subject = new RandomizeIfEmptyProps(fakeProps)

        when:
        def actual = subject.env(nonExistentPropName)

        then:
        actual != null
        actual.name() == nonExistentPropName
        actual.value() != null
        actual.stringValue() != null
    }

    def "if non null property exists, then should return it"() {
        given:
        def fakeProps = new FakeProps()
        def subject = new RandomizeIfEmptyProps(fakeProps)
        fakeProps.add(existingPropName, existingPropValue)

        when:
        def actual = subject.nonnull(existingPropName)

        then:
        actual != null
        actual.name() == existingPropName
        actual.value() == existingPropValue
        actual.stringValue() == existingPropValue
    }


    def "if nullable property exists, then should return it"() {
        given:
        def fakeProps = new FakeProps()
        def subject = new RandomizeIfEmptyProps(fakeProps)
        fakeProps.add(existingPropName, existingPropValue)

        when:
        def actual = subject.nullable(existingPropName)

        then:
        actual != null
        actual.name() == existingPropName
        actual.value() == existingPropValue
        actual.stringValue() == existingPropValue
    }

    def "if system environment property exists, then should return it"() {
        given:
        def fakeProps = new FakeProps()
        def subject = new RandomizeIfEmptyProps(fakeProps)
        fakeProps.addEnv(existingPropName, existingPropValue)

        when:
        def actual = subject.env(existingPropName)

        then:
        actual != null
        actual.name() == existingPropName
        actual.value() == existingPropValue
        actual.stringValue() == existingPropValue
    }
}
