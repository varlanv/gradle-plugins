package io.huskit.containers.model.port

import io.huskit.gradle.commontest.BaseUnitSpec

class DynamicContainerPortSpec extends BaseUnitSpec {

    def "should allocate random port"() {
        given:
        def port = new DynamicContainerPort()

        when:
        def portNumber = port.number()

        then:
        portNumber > 0
    }

    def "port should be accessible after picking"() {
        given:
        def port = new DynamicContainerPort()

        when:
        def portNumber = port.number()

        then:
        try (def socket = new ServerSocket(portNumber)) {
            socket.isBound()
        }
    }
}
