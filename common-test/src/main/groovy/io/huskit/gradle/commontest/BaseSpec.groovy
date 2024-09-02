package io.huskit.gradle.commontest

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

abstract class BaseSpec extends Specification {

    private static ObjectMapper MAPPER = new ObjectMapper()

    protected ObjectMapper mapper() {
        return MAPPER
    }
}
